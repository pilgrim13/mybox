package com.mybox.mybox.file.service;

import com.mybox.mybox.common.FileResponse;
import com.mybox.mybox.common.FileUtils;
import com.mybox.mybox.config.AwsProperties;
import com.mybox.mybox.exception.DownloadFailedException;
import com.mybox.mybox.file.domain.AWSS3Object;
import com.mybox.mybox.file.domain.UploadStatus;
import com.sun.istack.NotNull;
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
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
//
//    /**
//     * {@inheritDoc}
//     */
//
//    public Mono<byte[]> getByteObject(@NotNull String key) {
//        log.debug("Fetching object as byte array from S3 bucket: {}, key: {}", s3ConfigProperties.getS3BucketName(), key);
//        return Mono.just(GetObjectRequest.builder().bucket(s3ConfigProperties.getS3BucketName()).key(key).build())
//            .map(it -> s3AsyncClient.getObject(it, AsyncResponseTransformer.toBytes()))
//            .flatMap(Mono::fromFuture)
//            .map(BytesWrapper::asByteArray);
//    }

    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadObject(String objectKey) {

        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(s3ConfigProperties.getS3BucketName())
            .key(objectKey)
            .build();

        return Mono
            .fromFuture(s3AsyncClient.getObject(request, AsyncResponseTransformer.toPublisher()))
            .map(response -> {
                checkResult(response.response());
                String filename = getMetadataItem(response.response(), "filename", objectKey);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, response.response().contentType())
                    .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.response().contentLength()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(Flux.from(response));
            });
    }

    /**
     * Lookup a metadata key in a case-insensitive way.
     *
     * @param sdkResponse
     * @param key
     * @param defaultValue
     * @return
     */
    private String getMetadataItem(GetObjectResponse sdkResponse, String key, String defaultValue) {
        for (Entry<String, String> entry : sdkResponse.metadata()
            .entrySet()) {
            if (entry.getKey()
                .equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }
    // Helper used to check return codes from an API call

    private static void checkResult(GetObjectResponse response) {
        SdkHttpResponse sdkResponse = response.sdkHttpResponse();
        if (sdkResponse != null && sdkResponse.isSuccessful()) {
            return;
        }
        throw new DownloadFailedException(response);
    }

    /**
     * {@inheritDoc}
     */
    public Mono<FileResponse> uploadObject(String path, FilePart filePart) {

        String filename = filePart.filename();

        Map<String, String> metadata = Map.of("filename", filename);
        // get media type
        MediaType mediaType = ObjectUtils.defaultIfNull(filePart.headers().getContentType(), MediaType.APPLICATION_OCTET_STREAM);

        CompletableFuture<CreateMultipartUploadResponse> s3AsyncClientMultipartUpload = s3AsyncClient
            .createMultipartUpload(CreateMultipartUploadRequest.builder()
                .contentType(mediaType.toString())
                .key(filename)
                .metadata(metadata)
                .bucket(s3ConfigProperties.getS3BucketName())
                .build());

        UploadStatus uploadStatus = new UploadStatus(Objects.requireNonNull(filePart.headers().getContentType()).toString(), filename);

        return Mono.fromFuture(s3AsyncClientMultipartUpload)
            .flatMapMany(response -> {
                // response error 체크
                FileUtils.checkSdkResponse(response);
                uploadStatus.setUploadId(response.uploadId());
                log.info("Upload object with ID={}", response.uploadId());
                // DataBuffer 로 쪼개기
                return filePart.content();
            })
            .bufferUntil(dataBuffer -> {
                // Collect incoming values into multiple List buffers that will be emitted by the resulting Flux each time the given predicate returns true.
                uploadStatus.addBuffered(dataBuffer.readableByteCount());

                if (uploadStatus.getBuffered() >= 5242880) {
                    log.info("BufferUntil - returning true, bufferedBytes={}, partCounter={}, uploadId={}",
                        uploadStatus.getBuffered(), uploadStatus.getPartCounter(), uploadStatus.getUploadId());

                    // reset buffer
                    uploadStatus.setBuffered(0);
                    return true;
                }

                return false;
            })
            .map(FileUtils::dataBufferToByteBuffer)
            // upload part
            .flatMap(byteBuffer -> uploadPartObject(uploadStatus, byteBuffer))
            .onBackpressureBuffer()
            .reduce(uploadStatus, (status, completedPart) -> {
                log.info("Completed: PartNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                (status).getCompletedParts().put(completedPart.partNumber(), completedPart);
                return status;
            })
            .flatMap(uploadStatus1 -> completeMultipartUpload(uploadStatus))
            .map(response -> {
                FileUtils.checkSdkResponse(response);
                log.info("upload result: {}", response.toString());
                return new FileResponse(filename, uploadStatus.getUploadId(), response.location(), uploadStatus.getContentType(), response.eTag());
            });
    }

    /**
     * Uploads a part in a multipart upload.
     */
    private Mono<CompletedPart> uploadPartObject(UploadStatus uploadStatus, ByteBuffer buffer) {
        final int partNumber = uploadStatus.getAddedPartCounter();
        log.info("UploadPart - partNumber={}, contentLength={}", partNumber, buffer.capacity());

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
                FileUtils.checkSdkResponse(uploadPartResult);
                log.info("UploadPart - complete: part={}, etag={}", partNumber, uploadPartResult.eTag());
                return CompletedPart.builder()
                    .eTag(uploadPartResult.eTag())
                    .partNumber(partNumber)
                    .build();
            });
    }

    /**
     * This method is called when a part finishes uploading. It's primary function is to verify the ETag of the part we just uploaded.
     */
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


    /**
     * {@inheritDoc}
     */
    public Mono<Void> deleteObject(@NotNull String key) {
        log.info("Delete Object with key: {}", key);
        return Mono.just(DeleteObjectRequest.builder().bucket(s3ConfigProperties.getS3BucketName()).key(key).build())
            .map(s3AsyncClient::deleteObject)
            .flatMap(Mono::fromFuture)
            .then();
    }
}