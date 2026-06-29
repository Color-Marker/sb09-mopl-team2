package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.content.batch.Tmdb.TmdbClient;
import com.sb09.sb09moplteam2.content.batch.Tmdb.TmdbMovieProcessor;
import com.sb09.sb09moplteam2.content.batch.Tmdb.TmdbMovieReader;
import com.sb09.sb09moplteam2.content.batch.Tmdb.TmdbMovieWriter;
import com.sb09.sb09moplteam2.content.batch.Tmdb.dto.TmdbMovieResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
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

  @Bean
  public Job tmdbMovieJob() {
    return new JobBuilder("tmdbMovieJob", jobRepository)
        .start(tmdbMovieStep())
        .next(tmdbDramaStep())
        .build();
  }

  @Bean
  public Step tmdbMovieStep() {
    return new StepBuilder("tmdbMovieStep", jobRepository)
        .<TmdbMovieResponse, Content>chunk(10, transactionManager)
        .reader(new TmdbMovieReader(tmdbClient, ContentType.movie))
        .processor(new TmdbMovieProcessor(contentRepository, ContentType.movie))
        .writer(new TmdbMovieWriter(contentRepository))
        .build();
  }

  @Bean
  public Step tmdbDramaStep() {
    return new StepBuilder("tmdbDramaStep", jobRepository)
        .<TmdbMovieResponse, Content>chunk(10, transactionManager)
        .reader(new TmdbMovieReader(tmdbClient, ContentType.tvSeries))
        .processor(new TmdbMovieProcessor(contentRepository, ContentType.tvSeries))
        .writer(new TmdbMovieWriter(contentRepository))
        .build();
  }
}