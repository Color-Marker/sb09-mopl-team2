package com.sb09.sb09moplteam2.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class GlobalBatchConfig {

  @Bean(name = "batchTaskExecutor")
  public TaskExecutor batchTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Mopl-Batch-");
    executor.initialize();
    return executor;
  }

  // JobBuilder에 .incrementer(globalRunIdIncrementer()) 로 가져다 씁니다.
  @Bean
  public RunIdIncrementer globalRunIdIncrementer() {
    return new RunIdIncrementer();
  }
}
