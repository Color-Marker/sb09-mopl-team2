package com.sb09.sb09moplteam2.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "mopl.storage.type", havingValue = "s3")
public class S3Config {

  @Bean
  public S3Client s3Client(StorageProperties storageProperties) {
    StorageProperties.S3 s3 = storageProperties.getS3();
    return S3Client.builder()
        .region(Region.of(s3.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())
            )
        )
        .build();
  }
}