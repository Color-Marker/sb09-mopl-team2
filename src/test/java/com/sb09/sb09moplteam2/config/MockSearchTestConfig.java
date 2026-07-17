package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockSearchTestConfig {

  @Bean
  public ContentSearchService contentSearchService() {
    return Mockito.mock(ContentSearchService.class);
  }
}