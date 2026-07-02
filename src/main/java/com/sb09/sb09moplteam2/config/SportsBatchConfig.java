package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.content.batch.Sports.SportsClient;
import com.sb09.sb09moplteam2.content.batch.Sports.SportsProcessor;
import com.sb09.sb09moplteam2.content.batch.Sports.SportsReader;
import com.sb09.sb09moplteam2.content.batch.Sports.SportsWriter;
import com.sb09.sb09moplteam2.content.batch.Sports.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SportsBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SportsClient sportsClient;
  private final ContentRepository contentRepository;

  @Bean
  public Job sportsJob() {
    return new JobBuilder("sportsJob", jobRepository)
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
        .build();
  }

  @Bean
  public SportsReader sportsReader() {
    return new SportsReader(sportsClient);
  }

  @Bean
  public SportsProcessor sportsProcessor() {
    return new SportsProcessor(contentRepository);
  }

  @Bean
  public SportsWriter sportsWriter() {
    return new SportsWriter(contentRepository);
  }
}