package com.sb09.sb09moplteam2.batch.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BatchJobMetricsListenerTest {

  private SimpleMeterRegistry meterRegistry;
  private BatchJobMetricsListener listener;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    listener = new BatchJobMetricsListener(meterRegistry);
  }

  private JobExecution createJobExecution(Long id, String jobName, BatchStatus status, LocalDateTime startTime, LocalDateTime endTime) {
    JobInstance jobInstance = new JobInstance(id, jobName);
    JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());
    jobExecution.setId(id);
    jobExecution.setStatus(status);
    jobExecution.setStartTime(startTime);
    jobExecution.setEndTime(endTime);
    return jobExecution;
  }

  @Test
  @DisplayName("배치 Job이 성공적으로 종료되면 성공 카운터와 소요 시간 메트릭이 기록된다.")
  void afterJob_Success_RecordsMetrics() {
    // given
    LocalDateTime now = LocalDateTime.now();
    JobExecution jobExecution = createJobExecution(1L, "testSyncJob", BatchStatus.COMPLETED, now.minusSeconds(1), now);

    // when
    listener.afterJob(jobExecution);

    // then
    Counter counter = meterRegistry.find("mopl.batch.job.total")
        .tag("job.name", "testSyncJob")
        .tag("status", "COMPLETED")
        .counter();

    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);

    Timer timer = meterRegistry.find("mopl.batch.job.duration")
        .tag("job.name", "testSyncJob")
        .tag("status", "COMPLETED")
        .timer();

    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1L);
    assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0);
  }

  @Test
  @DisplayName("배치 Job이 실패해도 실패 상태로 메트릭이 정확히 기록된다.")
  void afterJob_Failed_RecordsFailedMetrics() {
    // given
    LocalDateTime now = LocalDateTime.now();
    JobExecution jobExecution = createJobExecution(2L, "testSyncJob", BatchStatus.FAILED, now.minusSeconds(2), now);

    // when
    listener.afterJob(jobExecution);

    // then
    Counter counter = meterRegistry.find("mopl.batch.job.total")
        .tag("job.name", "testSyncJob")
        .tag("status", "FAILED")
        .counter();

    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  @DisplayName("시간 정보가 누락된 경우 타이머 메트릭을 기록하지 않고 넘어간다.")
  void afterJob_MissingTime_DoesNotRecordDuration() {
    // given
    JobExecution jobExecution = createJobExecution(3L, "testSyncJob", BatchStatus.COMPLETED, null, null);

    // when
    listener.afterJob(jobExecution);

    // then
    Counter counter = meterRegistry.find("mopl.batch.job.total").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);

    Timer timer = meterRegistry.find("mopl.batch.job.duration").timer();
    assertThat(timer).isNull();
  }
}
