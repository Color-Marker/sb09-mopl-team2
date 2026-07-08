package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.batch.listener.GlobalStepExceptionListener;
import com.sb09.sb09moplteam2.batch.monitoring.BatchJobMetricsListener;
import com.sb09.sb09moplteam2.content.batch.sport.SportClient;
import com.sb09.sb09moplteam2.content.batch.sport.SportProcessor;
import com.sb09.sb09moplteam2.content.batch.sport.SportReader;
import com.sb09.sb09moplteam2.content.batch.sport.SportWriter;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
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
public class SportBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SportClient sportClient;
  private final ContentRepository contentRepository;
  private final BatchJobMetricsListener batchJobMetricsListener;
  private final GlobalStepExceptionListener globalStepExceptionListener;
  private final RunIdIncrementer globalRunIdIncrementer;

  @Bean
  public Job sportsJob() {
    return new JobBuilder("sportsJob", jobRepository)
        .incrementer(globalRunIdIncrementer)
        .listener(batchJobMetricsListener)
        .start(sportsStep())
        .build();
  }

  @Bean
  public Step sportsStep() {
    return new StepBuilder("sportsStep", jobRepository)
        .<SportsEventResponse, Content>chunk(10, transactionManager)
        .reader(sportsReader())
        .processor(sportsProcessor())
        .writer(sportsWriter())
        .listener(globalStepExceptionListener)
        .build();
  }

  @Bean
  public SportReader sportsReader() {
    return new SportReader(sportClient);
  }

  @Bean
  public SportProcessor sportsProcessor() {
    return new SportProcessor(contentRepository);
  }

  @Bean
  public SportWriter sportsWriter() {
    return new SportWriter(contentRepository);
  }
}