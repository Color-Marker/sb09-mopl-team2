package com.sb09.sb09moplteam2.batch.sport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sb09.sb09moplteam2.content.batch.sport.SportScheduler;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;


@ExtendWith(MockitoExtension.class)
class SportSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job sportsJob;

  @Mock
  private RedissonClient redissonClient;

  @Mock
  private RLock rLock;

  @Test
  @DisplayName("락을 획득하면 배치 작업이 정상적으로 실행된다")
  void runSportsBatch_락_획득시_정상적으로_실행된다() throws Exception {

    SportScheduler scheduler = new SportScheduler(jobLauncher, sportsJob, redissonClient);
    given(redissonClient.getLock("batch-lock:sports")).willReturn(rLock);
    given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
    given(rLock.isHeldByCurrentThread()).willReturn(true);
    given(jobLauncher.run(any(Job.class), any(JobParameters.class)))
        .willReturn(mock(JobExecution.class));

    scheduler.runSportsBatch();

    then(jobLauncher).should().run(any(Job.class), any(JobParameters.class));
    then(rLock).should().unlock();
  }

  @Test
  @DisplayName("락 획득에 실패하면 배치 작업을 실행하지 않고 스킵한다")
  void runSportsBatch_락_획득_실패시_스킵한다() throws Exception {
    SportScheduler scheduler = new SportScheduler(jobLauncher, sportsJob, redissonClient);
    given(redissonClient.getLock("batch-lock:sports")).willReturn(rLock);
    given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);

    scheduler.runSportsBatch();

    then(jobLauncher).should(never()).run(any(Job.class), any(JobParameters.class));
    then(rLock).should(never()).unlock();
  }

  @Test
  @DisplayName("배치 실행 중 예외가 발생해도 예외를 전파하지 않는다")
  void runSportsBatch_예외_발생시_전파하지_않는다() throws Exception {
    SportScheduler scheduler = new SportScheduler(jobLauncher, sportsJob, redissonClient);
    given(redissonClient.getLock("batch-lock:sports")).willReturn(rLock);
    given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
    given(rLock.isHeldByCurrentThread()).willReturn(true);
    willThrow(new JobExecutionAlreadyRunningException("이미 실행 중"))
        .given(jobLauncher).run(any(Job.class), any(JobParameters.class));

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(scheduler::runSportsBatch);

    then(rLock).should().unlock();
  }

  @Test
  @DisplayName("락 대기 중 인터럽트가 발생해도 예외를 전파하지 않는다")
  void runSportsBatch_인터럽트_발생시_전파하지_않는다() throws Exception {
    SportScheduler scheduler = new SportScheduler(jobLauncher, sportsJob, redissonClient);
    given(redissonClient.getLock("batch-lock:sports")).willReturn(rLock);
    willThrow(new InterruptedException("인터럽트"))
        .given(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class));

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(scheduler::runSportsBatch);

    then(jobLauncher).should(never()).run(any(Job.class), any(JobParameters.class));
  }
}