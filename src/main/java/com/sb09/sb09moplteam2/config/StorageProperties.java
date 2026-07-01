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

  @Getter
  @Setter
  public static class Local {
    private String rootPath;
  }
}