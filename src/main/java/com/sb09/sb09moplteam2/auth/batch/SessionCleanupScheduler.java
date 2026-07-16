package com.sb09.sb09moplteam2.auth.batch;

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
public class SessionCleanupScheduler {

  private final JobLauncher jobLauncher;
  private final Job sessionCleanupJob;

  @Scheduled(cron = "0 30 4 * * *")
  public void runSessionCleanupBatch() {
    try {
      log.info("세션 정리 배치 작업 시작");
      JobParameters params = new JobParametersBuilder()
          .addLocalDateTime("runTime", LocalDateTime.now())
          .toJobParameters();
      jobLauncher.run(sessionCleanupJob, params);
      log.info("세션 정리 배치 작업 완료");
    } catch (Exception e) {
      log.error("세션 정리 배치 작업 실패", e);
    }
  }
}
