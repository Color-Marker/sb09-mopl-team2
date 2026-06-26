package com.sb09.sb09moplteam2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mopl.admin")
@Getter
@Setter
public class AdminProperties {

  private String username;
  private String email;
  private String password;
}