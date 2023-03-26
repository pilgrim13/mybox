package com.mybox.mybox.file.service;

import com.mybox.mybox.common.FileResponse;
import com.mybox.mybox.common.FileUtils;
import com.mybox.mybox.config.AwsProperties;
import com.mybox.mybox.exception.UploadFailedException;
import com.mybox.mybox.file.domain.AWSS3Object;
import com.mybox.mybox.file.domain.UploadStatus;
import com.sun.istack.NotNull;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final S3AsyncClient s3AsyncClient;
    private final AwsProperties s3ConfigProperties;

    public Flux<AWSS3Object> getObjects() {
        log.info("Listing objects in S3 bucket: {}", s3ConfigProperties.getS3BucketName());
        return Flux.from(s3AsyncClient.listObjectsV2Paginator(ListObjectsV2Request.builder()
                .bucket(s3ConfigProperties.getS3BucketName())
                .build()))
            .flatMap(response -> Flux.fromIterable(response.contents()))
            .map(s3Object -> new AWSS3Object(s3Object.key(), s3Object.lastModified(), s3Object.eTag(), s3Object.size()));
    }

    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadObject(String objectKey) {

        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(s3ConfigProperties.getS3BucketName())
            .key(objectKey)
            .build();

        return Mono
            .fromFuture(s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()))
            .map(response -> {
                checkResult(response.response());
                String filename = getMetadataItem(response.response(), objectKey);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, response.response().contentType())
                    .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.response().contentLength()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(Flux.from(response));
            });
    }

    private String getMetadataItem(GetObjectResponse sdkResponse, String defaultValue) {
        for (Entry<String, String> entry : sdkResponse.metadata()
            .entrySet()) {
            if (entry.getKey()
                .equalsIgnoreCase("filename")) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }

    public Mono<FileResponse> uploadObjectWithMultiPart(String path, FilePart filePart) {

        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String filename = path + filePart.filename();
        Map<String, String> metadata = new java.util.HashMap<>(Map.of("filename", filename));

        // get media type
        MediaType mediaType = ObjectUtils.defaultIfNull(filePart.headers().getContentType(), MediaType.APPLICATION_OCTET_STREAM);

        // 1. MultiPart Upload 시작 ( Upload Id 요청 )
        CompletableFuture<CreateMultipartUploadResponse> s3AsyncClientMultipartUpload = s3AsyncClient
            .createMultipartUpload(CreateMultipartUploadRequest.builder()
                .contentType(mediaType.toString())
                .key(filename)
                .metadata(metadata)
                .bucket(s3ConfigProperties.getS3BucketName())
                .build());

        // 파일 업로드를 위한 상태 객체
        UploadStatus uploadStatus = new UploadStatus(Objects.requireNonNull(filePart.headers().getContentType()).toString(), filename);

        return Mono.fromFuture(s3AsyncClientMultipartUpload)
            .flatMapMany(response -> {
                // response error 체크
                checkResult(response);
                uploadStatus.setUploadId(response.uploadId());
                log.info("Upload object with ID={}", response.uploadId());
                // Flux DataBuffer 로 쪼개기
                return filePart.content();
            })
            .bufferUntil(dataBuffer -> {
                // 바이트 수 축적
                uploadStatus.addBuffered(dataBuffer.readableByteCount());

                // 일정 크기가 되면 업로드 상태 객체의 누적 바이트 수를 0으로 초기화 후 return true
                if (uploadStatus.getBuffered() >= 5242880) {
                    log.info("BufferUntil - returning true, bufferedBytes={}, partCounter={}, uploadId={}",
                        uploadStatus.getBuffered(), uploadStatus.getPartCounter(), uploadStatus.getUploadId());
                    uploadStatus.setBuffered(0);
                    return true;
                }
                return false;
            })
            // 업로드를 위한 버퍼 형변환
            .map(FileUtils::dataBufferToByteBuffer)
            // 2. 부분 업로드 진행
            .flatMap(byteBuffer -> uploadPartObject(uploadStatus, byteBuffer))
            // 배압 컨트롤 하여 버퍼 모으기
            .onBackpressureBuffer()
            // Flux to Mono
            .reduce(uploadStatus, (status, completedPart) -> {
                log.info("Completed: PartNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                (status).getCompletedParts().put(completedPart.partNumber(), completedPart);
                return status;
            })
            // 3. 업로드 완료 처리
            .flatMap(status -> completeMultipartUpload(uploadStatus))
            .map(response -> {
                checkResult(response);
                log.info("upload result: {}", response);
                return new FileResponse(filename, uploadStatus.getUploadId(), response.location(), uploadStatus.getContentType(), response.eTag());
            });
    }

    private Mono<CompletedPart> uploadPartObject(UploadStatus uploadStatus, ByteBuffer buffer) {
        // 부분 업로드 파트 식별 값 초기화
        final int partNumber = uploadStatus.getAddedPartCounter();
        log.info("UploadPart - partNumber={}, contentLength={}", partNumber, buffer.capacity());

        // 부분 업로드 진행
        CompletableFuture<UploadPartResponse> uploadPartResponseCompletableFuture = s3AsyncClient.uploadPart(UploadPartRequest.builder()
                .bucket(s3ConfigProperties.getS3BucketName())
                .key(uploadStatus.getFileKey())
                .partNumber(partNumber)
                .uploadId(uploadStatus.getUploadId())
                .contentLength((long) buffer.capacity())
                .build(),
            AsyncRequestBody.fromPublisher(Mono.just(buffer)));

        return Mono
            .fromFuture(uploadPartResponseCompletableFuture)
            .map(uploadPartResult -> {
                checkResult(uploadPartResult);
                log.info("UploadPart - complete: part={}, etag={}", partNumber, uploadPartResult.eTag());
                return CompletedPart.builder()
                    .eTag(uploadPartResult.eTag())
                    .partNumber(partNumber)
                    .build();
            });
    }

    // 멀티 파트 업로드 완료처리

    private Mono<CompleteMultipartUploadResponse> completeMultipartUpload(UploadStatus uploadStatus) {
        log.info("CompleteUpload - fileKey={}, completedParts.size={}",
            uploadStatus.getFileKey(), uploadStatus.getCompletedParts().size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
            .parts(uploadStatus.getCompletedParts().values())
            .build();

        return Mono.fromFuture(s3AsyncClient.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
            .bucket(s3ConfigProperties.getS3BucketName())
            .uploadId(uploadStatus.getUploadId())
            .multipartUpload(multipartUpload)
            .key(uploadStatus.getFileKey())
            .build()));
    }

    public Mono<Void> deleteObject(@NotNull String key) {
        log.info("Delete Object with key: {}", key);
        return Mono.just(DeleteObjectRequest.builder().bucket(s3ConfigProperties.getS3BucketName()).key(key).build())
            .map(s3AsyncClient::deleteObject)
            .flatMap(Mono::fromFuture)
            .then();
    }

    private static void checkResult(SdkResponse result) {
        if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful()) {
            throw new UploadFailedException(result);
        }
    }
}