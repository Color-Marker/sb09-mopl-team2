package com.sb09.sb09moplteam2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정 클래스
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${mopl.storage.local.root-path:.mopl/storage}")
  private String storagePath;

  @Bean
  public MDCLoggingInterceptor mdcLoggingInterceptor() {
    return new MDCLoggingInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(mdcLoggingInterceptor())
        .addPathPatterns("/**"); // 모든 경로에 적용
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/files/**")
        .addResourceLocations("file:" + storagePath + "/");
  }
}