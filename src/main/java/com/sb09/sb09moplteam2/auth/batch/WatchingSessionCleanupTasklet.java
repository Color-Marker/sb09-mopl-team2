package com.sb09.sb09moplteam2.auth.batch;

import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * 유령 시청 세션 정리 태스크릿.
 * 연결 종료(Disconnect) 이벤트가 유실되면(재배포로 인한 매핑 소실 등) ACTIVE 세션이
 * DB에 영구히 남아 시청자 목록에 유령으로 표시되므로, 오래된 ACTIVE 세션을 일괄 종료한다.
 */
@Slf4j
@RequiredArgsConstructor
public class WatchingSessionCleanupTasklet implements Tasklet {

  // 이 시간 이상 지속된 ACTIVE 세션은 유령으로 간주 (정상 시청은 하트비트 종료 처리로 관리됨)
  private static final Duration STALE_THRESHOLD = Duration.ofHours(6);

  private final WatchingSessionRepository watchingSessionRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    Instant now = Instant.now();
    int ended = watchingSessionRepository.endAllActiveStartedBefore(
        now.minus(STALE_THRESHOLD), now);
    contribution.incrementWriteCount(ended);
    log.info("[SESSION CLEANUP] 오래된 ACTIVE 시청 세션 {}건 종료", ended);
    return RepeatStatus.FINISHED;
  }
}
