package com.sb09.sb09moplteam2.batch.config;

import com.sb09.sb09moplteam2.batch.monitoring.BatchJobMetricsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GlobalBatchConfig {

  private final BatchJobMetricsListener batchJobMetricsListener;

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

  //JobBuilder에 .incrementer(globalRunIdIncrementer()) 로 가져다 씁니다.
  @Bean
  public RunIdIncrementer globalRunIdIncrementer() {
    return new RunIdIncrementer();
  }

  //StepBuilder에 .listener(globalStepExceptionListener()) 로 가져다 씁니다.
  @Bean
  public StepExecutionListener globalStepExceptionListener() {
    return new StepExecutionListener() {
      @Override
      public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getStatus().isUnsuccessful()) {
          log.error("[GLOBAL BATCH ERROR] 🚨 Step 실패 감지!");
          log.error("- 실패한 Job: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
          log.error("- 실패한 Step: {}", stepExecution.getStepName());
          log.error("- 실패 원인: {}", stepExecution.getFailureExceptions());
        }
        return stepExecution.getExitStatus();
      }
    };
  }
}
