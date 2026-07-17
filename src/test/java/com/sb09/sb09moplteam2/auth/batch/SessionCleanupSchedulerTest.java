package com.sb09.sb09moplteam2.auth.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class SessionCleanupSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job sessionCleanupJob;

  @Test
  @DisplayName("세션 정리 배치 작업이 정상적으로 실행된다")
  void runSessionCleanupBatch_정상적으로_실행된다() throws Exception {
    SessionCleanupScheduler scheduler = new SessionCleanupScheduler(jobLauncher, sessionCleanupJob);
    given(jobLauncher.run(any(Job.class), any(JobParameters.class)))
        .willReturn(mock(JobExecution.class));

    scheduler.runSessionCleanupBatch();

    then(jobLauncher).should().run(any(Job.class), any(JobParameters.class));
  }

  @Test
  @DisplayName("배치 실행 중 예외가 발생해도 예외를 전파하지 않는다")
  void runSessionCleanupBatch_예외_발생시_전파하지_않는다() throws Exception {
    SessionCleanupScheduler scheduler = new SessionCleanupScheduler(jobLauncher, sessionCleanupJob);
    willThrow(new JobExecutionAlreadyRunningException("이미 실행 중"))
        .given(jobLauncher).run(any(Job.class), any(JobParameters.class));

    Assertions.assertDoesNotThrow(scheduler::runSessionCleanupBatch);
  }
}
