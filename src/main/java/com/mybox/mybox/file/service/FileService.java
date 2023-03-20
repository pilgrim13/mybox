package com.mybox.mybox.file.service;

import com.mybox.mybox.common.FileResponse;
import com.mybox.mybox.file.domain.AWSS3Object;
import com.sun.istack.NotNull;
import java.nio.ByteBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {

    Mono<FileResponse> uploadObject(String path, FilePart filePart);

    Flux<AWSS3Object> getObjects();

//    Mono<byte[]> getByteObject(@NotNull String key);

    Mono<ResponseEntity<Flux<ByteBuffer>>> downloadObject(String objectKey);

    Mono<Void> deleteObject(@NotNull String objectKey);
}