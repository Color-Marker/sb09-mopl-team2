package com.sb09.sb09moplteam2.config;

import org.opensearch.data.client.osc.OpenSearchConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.lang.NonNull;

import java.time.Duration;

@Configuration
public class OpenSearchClientConfig extends OpenSearchConfiguration {

  @Value("${opensearch.endpoint}")
  private String endpoint;

  @Value("${opensearch.username:}")
  private String username;

  @Value("${opensearch.password:}")
  private String password;

  @Value("${opensearch.ssl:false}")
  private boolean ssl;

  @NonNull
  @Override
  public ClientConfiguration clientConfiguration() {
    if (ssl && !username.isBlank()) {
      return ClientConfiguration.builder()
          .connectedTo(endpoint)
          .usingSsl()
          .withBasicAuth(username, password)
          .withConnectTimeout(Duration.ofSeconds(10))
          .withSocketTimeout(Duration.ofSeconds(5))
          .build();
    }
    return ClientConfiguration.builder()
        .connectedTo(endpoint)
        .withConnectTimeout(Duration.ofSeconds(10))
        .withSocketTimeout(Duration.ofSeconds(5))
        .build();
  }
}