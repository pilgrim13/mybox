//package com.mybox.mybox.config;
//
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.client.builder.AwsClientBuilder;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ObjectStorageConfig {
//
//    final String endPoint = "https://kr.object.ncloudstorage.com";
//    final String regionName = "kr-standard";
//    @Value("${aws.s3.access-token}")
//    String accessKey;
//    @Value("${aws.s3.secret-token}")
//    String secretKey;
//
//    @Bean
//    AmazonS3 getAmazonS3() {
//        return AmazonS3ClientBuilder.standard()
//            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
//            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
//            .build();
//    }
//
//}