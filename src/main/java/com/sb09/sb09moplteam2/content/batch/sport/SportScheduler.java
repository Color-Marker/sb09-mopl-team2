package com.sb09.sb09moplteam2.content.batch.sport;

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
public class SportScheduler {

  private final JobLauncher jobLauncher;
  private final Job sportsJob;

  @PostConstruct
  public void runOnStartup() {
    runSportsBatch();
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void runSportsBatch() {
    try {
      log.info("Sports 배치 작업 시작");
      JobParameters params = new JobParametersBuilder()
          .addLocalDateTime("runTime", LocalDateTime.now())
          .toJobParameters();
      jobLauncher.run(sportsJob, params);
      log.info("Sports 배치 작업 완료");
    } catch (Exception e) {
      log.error("Sports 배치 작업 실패", e);
    }
  }
}