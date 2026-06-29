package com.sb09.sb09moplteam2.batch.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobMetricsListener implements JobExecutionListener {

  private final MeterRegistry meterRegistry;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();
    log.info("[GLOBAL BATCH] ▶▶▶ [{}] 배치 Job 생명주기 시작. 시스템 모니터링을 개시합니다.", jobName);
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();
    BatchStatus status = jobExecution.getStatus();

    log.info("[GLOBAL BATCH] ■■■ [{}] 배치 Job 종료 감지. 상태: [{}]", jobName, status);

    // 📊 1. 커스텀 카운터 메트릭 (Job 실행 횟수 및 결과 기록)
    // Actuator 엔드포인트: /actuator/metrics/mopl.batch.job.total
    Counter.builder("mopl.batch.job.total")
        .description("MOPL 서비스 배치 Job 누적 실행 횟수")
        .tag("job.name", jobName)
        .tag("status", status.name())
        .register(meterRegistry)
        .increment();

    // ⏱️ 2. 커스텀 타이머 메트릭 (Job 실행 소요 시간 정밀 측정)
    // Actuator 엔드포인트: /actuator/metrics/mopl.batch.job.duration
    if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
      long durationMillis = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();

      Timer.builder("mopl.batch.job.duration")
          .description("MOPL 서비스 배치 Job 실행 소요 시간")
          .tag("job.name", jobName)
          .tag("status", status.name())
          .register(meterRegistry)
          .record(durationMillis, TimeUnit.MILLISECONDS);

      log.info("[GLOBAL BATCH] 📈 메트릭 적재 완료. 소요시간: {}ms", durationMillis);
    } else {
      log.warn("[GLOBAL BATCH] ⚠️ Job 시작/종료 시간 정보가 누락되어 시간 메트릭을 적재하지 못했습니다.");
    }
  }
}
