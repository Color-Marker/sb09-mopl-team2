package com.sb09.sb09moplteam2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mopl.jwt")
@Getter
@Setter
public class JwtProperties {

  private Token accessToken = new Token();
  private Token refreshToken = new Token();

  @Getter
  @Setter
  public static class Token {
    private String secret;
    private long expirationMs;
  }
}