package com.mybox.mybox.file.controller;

import com.mybox.mybox.common.FileResponse;
import com.mybox.mybox.file.domain.AWSS3Object;
import com.mybox.mybox.file.service.FileService;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    //single file upload
    @PostMapping
    public Mono<FileResponse> uploadFile(@RequestPart("file") FilePart filePart) {
        return fileService.uploadObject(filePart);
    }

    @GetMapping
    public Flux<AWSS3Object> getFiles() {
        return fileService.getObjects();
    }

    @GetMapping("/{objectKey}")
    public Mono<byte[]> getFile(@PathVariable String objectKey) {
        return fileService.getByteObject(objectKey);
    }

    @GetMapping("/v2/{objectKey}")
    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(@PathVariable String objectKey) {
        return fileService.downloadObject(objectKey);
    }

    @DeleteMapping("/{objectKey}")
    public Mono<Void> deleteFile(@PathVariable String objectKey) {
        return fileService.deleteObject(objectKey);
    }

}