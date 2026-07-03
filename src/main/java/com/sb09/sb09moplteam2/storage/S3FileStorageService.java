package com.sb09.sb09moplteam2.storage;

import com.sb09.sb09moplteam2.config.StorageProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mopl.storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

  private final S3Client s3Client;
  private final StorageProperties storageProperties;

  @Override
  public String store(MultipartFile file) {
    try {
      StorageProperties.S3 s3 = storageProperties.getS3();
      String key = "profiles/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(s3.getBucket())
          .key(key)
          .contentType(file.getContentType())
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

      return "https://" + s3.getBucket() + ".s3." + s3.getRegion() + ".amazonaws.com/" + key;
    } catch (Exception e) {
      throw new IllegalStateException("S3 파일 업로드에 실패했습니다.", e);
    }
  }
}