package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.batch.listener.GlobalStepExceptionListener;
import com.sb09.sb09moplteam2.batch.monitoring.BatchJobMetricsListener;
import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbClient;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieProcessor;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieReader;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieWriter;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbPagePartitioner;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TmdbBatchConfig {

  private static final int GRID_SIZE = 10;
  private static final int MAX_PAGES = 500; // 배치 1회당 최대 수집 페이지

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
        .start(tmdbMovieMasterStep())
        .next(tmdbTvSeriesMasterStep())
        .build();
  }

  @Bean
  public Step tmdbMovieMasterStep() {
    return new StepBuilder("tmdbMovieMasterStep", jobRepository)
        .partitioner("tmdbMovieStep", new TmdbPagePartitioner(tmdbClient, ContentType.movie, MAX_PAGES))
        .partitionHandler(tmdbMoviePartitionHandler())
        .build();
  }

  @Bean
  public Step tmdbMovieStep() {
    return new StepBuilder("tmdbMovieStep", jobRepository)
        .<TmdbEventResponse, ContentAndTags>chunk(100, transactionManager)
        .reader(tmdbMovieReader(null, null, null))
        .processor(new TmdbMovieProcessor(contentRepository, ContentType.movie))
        .writer(new TmdbMovieWriter(contentRepository, contentTagRepository))
        .listener(globalStepExceptionListener)
        .build();
  }

  @StepScope
  @Bean
  public TmdbMovieReader tmdbMovieReader(
      @Value("#{stepExecutionContext['startPage']}") Integer startPage,
      @Value("#{stepExecutionContext['endPage']}") Integer endPage,
      @Value("#{stepExecutionContext['partitionName']}") String partitionName){
    return new TmdbMovieReader(tmdbClient, ContentType.movie, startPage, endPage, partitionName);
  }

  @Bean
  public PartitionHandler tmdbMoviePartitionHandler() {
    return buildPartitionHandler(tmdbMovieStep());
  }

  @Bean
  public Step tmdbTvSeriesMasterStep() {
    return new StepBuilder("tmdbTvSeriesMasterStep", jobRepository)
        .partitioner("tmdbTvSeriesStep", new TmdbPagePartitioner(tmdbClient, ContentType.tvSeries, MAX_PAGES))
        .partitionHandler(tmdbTvSeriesPartitionHandler())
        .build();
  }

  @Bean
  public Step tmdbTvSeriesStep() {
    return new StepBuilder("tmdbTvSeriesStep", jobRepository)
        .<TmdbEventResponse, ContentAndTags>chunk(100, transactionManager)
        .reader(tmdbTvSeriesReader(null, null, null))
        .processor(new TmdbMovieProcessor(contentRepository, ContentType.tvSeries))
        .writer(new TmdbMovieWriter(contentRepository, contentTagRepository))
        .listener(globalStepExceptionListener)
        .build();
  }

  @StepScope
  @Bean
  public TmdbMovieReader tmdbTvSeriesReader(
      @Value("#{stepExecutionContext['startPage']}") Integer startPage,
      @Value("#{stepExecutionContext['endPage']}") Integer endPage,
      @Value("#{stepExecutionContext['partitionName']}") String partitionName) {
    return new TmdbMovieReader(tmdbClient, ContentType.tvSeries, startPage, endPage, partitionName);
  }

  @Bean
  public PartitionHandler tmdbTvSeriesPartitionHandler() {
    return buildPartitionHandler(tmdbTvSeriesStep());
  }

  private PartitionHandler buildPartitionHandler(Step workerStep) {
    TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
    handler.setStep(workerStep);
    handler.setTaskExecutor(partitionTaskExecutor());
    handler.setGridSize(GRID_SIZE);
    return handler;
  }

  @Bean
  public TaskExecutor partitionTaskExecutor() {
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("tmdb-partition-");
    executor.setConcurrencyLimit(GRID_SIZE);
    return executor;
  }
}