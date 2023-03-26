package com.mybox.mybox.folder.service;

import com.mybox.mybox.config.AwsProperties;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final S3AsyncClient s3AsyncClient;
    private final AwsProperties s3ConfigProperties;

    public void createFolder(String folderName) {

        if (!folderName.endsWith("/")) {
            folderName = folderName + "/";
        }

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(s3ConfigProperties.getS3BucketName())
            .key(folderName)
            .build();

        CompletableFuture<PutObjectResponse> future = s3AsyncClient.putObject(request, AsyncRequestBody.empty());
        future.whenComplete((response, throwable) -> {
            try {
                if (response != null) {
                    System.out.println("Object uploaded. Details: " + response);
                } else {
                    // Handle error
                    throwable.printStackTrace();
                }
            } finally {
                // Only close the client when you are completely done with it
                s3AsyncClient.close();
            }
        });
        future.join();
    }
}