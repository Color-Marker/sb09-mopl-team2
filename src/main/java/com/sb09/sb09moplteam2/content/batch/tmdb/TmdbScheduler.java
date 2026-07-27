package com.sb09.sb09moplteam2.content.batch.tmdb;

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
public class TmdbScheduler {

  private static final String LOCK_KEY = "batch-lock:tmdb";

  private final JobLauncher jobLauncher;
  private final Job tmdbEventJob;
  private final RedissonClient redissonClient;

  @Scheduled(cron = "0 0 0 * * *")
  public void runTmdbBatch() {
    RLock lock = redissonClient.getLock(LOCK_KEY);
    boolean acquired = false;
    try {
      // 최대 5초 대기하여 락 획득 시도, 획득 시 최대 30분간 락 유지(배치 소요 시간 감안)
      acquired = lock.tryLock(5, 1800, TimeUnit.SECONDS);
      if (!acquired) {
        log.info("TMDB 배치: 다른 인스턴스에서 이미 실행 중이라 스킵합니다");
        return;
      }

      log.info("TMDB 배치 작업 시작");
      JobParameters params = new JobParametersBuilder()
          .addLocalDateTime("runTime", LocalDateTime.now())
          .toJobParameters();
      jobLauncher.run(tmdbEventJob, params);
      log.info("TMDB 배치 작업 완료");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("TMDB 배치 락 획득 중 인터럽트 발생", e);
    } catch (Exception e) {
      log.error("TMDB 배치 작업 실패", e);
    } finally {
      if (acquired && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}