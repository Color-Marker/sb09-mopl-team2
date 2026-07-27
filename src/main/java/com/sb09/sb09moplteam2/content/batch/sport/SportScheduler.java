package com.sb09.sb09moplteam2.content.batch.sport;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

  private static final String LOCK_KEY = "batch-lock:sports";

  private final JobLauncher jobLauncher;
  private final Job sportsJob;
  private final RedissonClient redissonClient;

  @Scheduled(cron = "0 0 0 * * *")
  public void runSportsBatch() {
    RLock lock = redissonClient.getLock(LOCK_KEY);
    boolean acquired = false;
    try {
      acquired = lock.tryLock(5, 1800, TimeUnit.SECONDS);
      if (!acquired) {
        log.info("Sports 배치: 다른 인스턴스에서 이미 실행 중이라 스킵합니다");
        return;
      }

      log.info("Sports 배치 작업 시작");
      JobParameters params = new JobParametersBuilder()
          .addLocalDateTime("runTime", LocalDateTime.now())
          .toJobParameters();
      jobLauncher.run(sportsJob, params);
      log.info("Sports 배치 작업 완료");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Sports 배치 락 획득 중 인터럽트 발생", e);
    } catch (Exception e) {
      log.error("Sports 배치 작업 실패", e);
    } finally {
      if (acquired && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}