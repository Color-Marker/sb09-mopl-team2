package com.sb09.sb09moplteam2.auth.batch;

import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class JwtSessionCleanupTasklet implements Tasklet {

  private final JwtSessionRepository jwtSessionRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    int deleted = jwtSessionRepository.deleteAllRevokedOrExpired(Instant.now());
    contribution.incrementWriteCount(deleted);
    log.info("[SESSION CLEANUP] 만료/폐기된 JWT 세션 {}건 삭제", deleted);
    return RepeatStatus.FINISHED;
  }
}
