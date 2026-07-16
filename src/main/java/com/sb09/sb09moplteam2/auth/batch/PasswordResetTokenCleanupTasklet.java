package com.sb09.sb09moplteam2.auth.batch;

import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class PasswordResetTokenCleanupTasklet implements Tasklet {

  private final PasswordResetTokenRepository passwordResetTokenRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    int deleted = passwordResetTokenRepository.deleteAllUsedOrExpired(Instant.now());
    contribution.incrementWriteCount(deleted);
    log.info("[SESSION CLEANUP] 사용/만료된 임시 비밀번호 토큰 {}건 삭제", deleted);
    return RepeatStatus.FINISHED;
  }
}
