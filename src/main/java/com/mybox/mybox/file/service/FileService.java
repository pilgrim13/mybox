package com.mybox.mybox.file.service;

import com.mybox.mybox.common.FileResponse;
import com.mybox.mybox.file.domain.AWSS3Object;
import com.sun.istack.NotNull;
import java.nio.ByteBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {

    Mono<FileResponse> uploadObject(FilePart filePart);

    /**
     * Returns some or all (up to 1,000) of the objects in a bucket.
     *
     * @return Flux of object key
     */
    Flux<AWSS3Object> getObjects();

    /**
     * Retrieves byte objects from Amazon S3.
     *
     * @param key object key
     * @return object byte[]
     */
//    Mono<byte[]> getByteObject(@NotNull String key);

    Mono<byte[]> getByteObject(@NotNull String key);

    Mono<ResponseEntity<Flux<ByteBuffer>>> downloadObject(String objectKey);

    /**
     * Delete multiple objects from a bucket
     *
     * @param objectKey object key
     */
    Mono<Void> deleteObject(@NotNull String objectKey);
}