package com.sb09.sb09moplteam2.content.batch.Tmdb;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbScheduler {

  private final JobLauncher jobLauncher;
  private final Job tmdbMovieJob;

  @Scheduled(cron = "0 0 0 * * *")
  public void runTmdbBatch() {
    try {
      log.info("TMDB 배치 작업 시작");
      JobParameters params = new JobParametersBuilder()
          .addLocalDateTime("runTime", LocalDateTime.now())
          .toJobParameters();
      jobLauncher.run(tmdbMovieJob, params);
      log.info("TMDB 배치 작업 완료");
    } catch (Exception e) {
      log.error("TMDB 배치 작업 실패", e);
    }
  }

  @PostConstruct //테스트를 위 어플리케이션 시작할때 TMDB API 시작
  public void runOnStartup() {
    runTmdbBatch();
  }
}