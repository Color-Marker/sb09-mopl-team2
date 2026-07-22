package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.auth.batch.JwtSessionCleanupTasklet;
import com.sb09.sb09moplteam2.auth.batch.PasswordResetTokenCleanupTasklet;
import com.sb09.sb09moplteam2.auth.batch.WatchingSessionCleanupTasklet;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import com.sb09.sb09moplteam2.batch.listener.GlobalStepExceptionListener;
import com.sb09.sb09moplteam2.batch.monitoring.BatchJobMetricsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SessionCleanupBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final JwtSessionRepository jwtSessionRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final WatchingSessionRepository watchingSessionRepository;
  private final BatchJobMetricsListener batchJobMetricsListener;
  private final GlobalStepExceptionListener globalStepExceptionListener;
  private final RunIdIncrementer globalRunIdIncrementer;

  @Bean
  public Job sessionCleanupJob() {
    return new JobBuilder("sessionCleanupJob", jobRepository)
        .incrementer(globalRunIdIncrementer)
        .listener(batchJobMetricsListener)
        .start(jwtSessionCleanupStep())
        .next(passwordResetTokenCleanupStep())
        .next(watchingSessionCleanupStep())
        .build();
  }

  @Bean
  public Step jwtSessionCleanupStep() {
    return new StepBuilder("jwtSessionCleanupStep", jobRepository)
        .tasklet(new JwtSessionCleanupTasklet(jwtSessionRepository), transactionManager)
        .listener(globalStepExceptionListener)
        .build();
  }

  @Bean
  public Step passwordResetTokenCleanupStep() {
    return new StepBuilder("passwordResetTokenCleanupStep", jobRepository)
        .tasklet(new PasswordResetTokenCleanupTasklet(passwordResetTokenRepository), transactionManager)
        .listener(globalStepExceptionListener)
        .build();
  }

  @Bean
  public Step watchingSessionCleanupStep() {
    return new StepBuilder("watchingSessionCleanupStep", jobRepository)
        .tasklet(new WatchingSessionCleanupTasklet(watchingSessionRepository), transactionManager)
        .listener(globalStepExceptionListener)
        .build();
  }
}
