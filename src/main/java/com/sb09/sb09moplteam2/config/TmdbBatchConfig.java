package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.batch.listener.GlobalStepExceptionListener;
import com.sb09.sb09moplteam2.batch.monitoring.BatchJobMetricsListener;
import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbClient;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieProcessor;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieReader;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieWriter;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TmdbBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final TmdbClient tmdbClient;
  private final ContentRepository contentRepository;
  private final ContentTagRepository contentTagRepository;
  private final BatchJobMetricsListener batchJobMetricsListener;
  private final GlobalStepExceptionListener globalStepExceptionListener;
  private final RunIdIncrementer globalRunIdIncrementer;

  @Bean
  public Job tmdbEventJob() {
    return new JobBuilder("tmdbEventJob", jobRepository)
        .incrementer(globalRunIdIncrementer)
        .listener(batchJobMetricsListener)
        .start(tmdbMovieStep())
        .next(tmdbTvSeriesStep())
        .build();
  }

  @Bean
  public Step tmdbMovieStep() {
    return new StepBuilder("tmdbMovieStep", jobRepository)
        .<TmdbEventResponse, ContentAndTags>chunk(100, transactionManager)
        .reader(new TmdbMovieReader(tmdbClient, ContentType.movie))
        .processor(new TmdbMovieProcessor(contentRepository, ContentType.movie))
        .writer(new TmdbMovieWriter(contentRepository, contentTagRepository))
        .listener(globalStepExceptionListener)
        .build();
  }

  @Bean
  public Step tmdbTvSeriesStep() {
    return new StepBuilder("tmdbTvSeriesStep", jobRepository)
        .<TmdbEventResponse, ContentAndTags>chunk(100, transactionManager)
        .reader(new TmdbMovieReader(tmdbClient, ContentType.tvSeries))
        .processor(new TmdbMovieProcessor(contentRepository, ContentType.tvSeries))
        .writer(new TmdbMovieWriter(contentRepository, contentTagRepository))
        .listener(globalStepExceptionListener)
        .build();
  }
}