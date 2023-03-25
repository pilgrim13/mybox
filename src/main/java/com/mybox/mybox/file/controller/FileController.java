package com.mybox.mybox.file.controller;

import com.mybox.mybox.common.FileResponse;
import com.mybox.mybox.file.domain.AWSS3Object;
import com.mybox.mybox.file.service.FileService;
import com.mybox.mybox.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public Mono<FileResponse> uploadFile(@AuthenticationPrincipal User user,
                                         @RequestPart(value = "file", required = false) FilePart filePart, @RequestPart(value = "path", required = false) String path) {
        if (path == null) {
            path = StringUtils.EMPTY;
        }
        return fileService.uploadObject(user.getHomeFolder() + path, filePart);
    }

    @GetMapping
    public Flux<AWSS3Object> getFiles() {
        return fileService.getObjects();
    }

//    @GetMapping("/{objectKey}")
//    public Mono<byte[]> getFile(@PathVariable String objectKey) {
//        return fileService.getByteObject(objectKey);
//    }

    @GetMapping("/{objectKey}")
    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(@PathVariable String objectKey) {
        return fileService.downloadObject(objectKey);
    }

    @DeleteMapping("/{objectKey}")
    public Mono<Void> deleteFile(@PathVariable String objectKey) {
        return fileService.deleteObject(objectKey);
    }

}