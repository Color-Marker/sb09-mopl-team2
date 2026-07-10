package com.sb09.sb09moplteam2.batch.tmdb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbScheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

@ExtendWith(MockitoExtension.class)
class TmdbSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job tmdbEventJob;

  @Test
  @DisplayName("배치 작업이 정상적으로 실행된다")
  void runTmdbBatch_정상적으로_실행된다()
      throws JobExecutionAlreadyRunningException, JobRestartException,
      JobInstanceAlreadyCompleteException, JobParametersInvalidException {

    TmdbScheduler scheduler = new TmdbScheduler(jobLauncher, tmdbEventJob);
    given(jobLauncher.run(any(Job.class), any(JobParameters.class)))
        .willReturn(mock(JobExecution.class));

    scheduler.runTmdbBatch();

    then(jobLauncher).should().run(any(Job.class), any(JobParameters.class));
  }

  @Test
  @DisplayName("배치 실행 중 예외가 발생해도 예외를 전파하지 않는다")
  void runTmdbBatch_예외_발생시_전파하지_않는다()
      throws JobExecutionAlreadyRunningException, JobRestartException,
      JobInstanceAlreadyCompleteException, JobParametersInvalidException {

    TmdbScheduler scheduler = new TmdbScheduler(jobLauncher, tmdbEventJob);
    willThrow(new JobExecutionAlreadyRunningException("이미 실행 중"))
        .given(jobLauncher).run(any(Job.class), any(JobParameters.class));

    Assertions.assertDoesNotThrow(scheduler::runTmdbBatch);
  }
}