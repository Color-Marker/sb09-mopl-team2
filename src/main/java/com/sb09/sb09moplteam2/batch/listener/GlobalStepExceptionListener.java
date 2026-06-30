package com.sb09.sb09moplteam2.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalStepExceptionListener implements StepExecutionListener {

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    if (stepExecution.getStatus().isUnsuccessful()) {
      log.error("[GLOBAL BATCH ERROR] Step 실패 감지!");
      log.error("- 실패한 Job: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
      log.error("- 실패한 Step: {}", stepExecution.getStepName());
      log.error("- 실패 원인: {}", stepExecution.getFailureExceptions());
    }
    return stepExecution.getExitStatus();
  }
}
