package com.sb09.sb09moplteam2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mopl.storage")
@Getter
@Setter
public class StorageProperties {

  private String type;
  private Local local = new Local();
  private S3 s3 = new S3();

  @Getter
  @Setter
  public static class Local {
    private String rootPath;
  }

  @Getter
  @Setter
  public static class S3 {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private long presignedUrlExpiration;
  }
}