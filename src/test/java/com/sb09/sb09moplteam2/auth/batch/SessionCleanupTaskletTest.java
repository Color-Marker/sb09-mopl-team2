package com.sb09.sb09moplteam2.auth.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class SessionCleanupTaskletTest {

  @Mock
  private JwtSessionRepository jwtSessionRepository;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private WatchingSessionRepository watchingSessionRepository;

  @Mock
  private StepExecution stepExecution;

  private StepContribution contribution() {
    return new StepContribution(stepExecution);
  }

  private ChunkContext chunkContext() {
    return new ChunkContext(new StepContext(stepExecution));
  }

  @Test
  @DisplayName("JWT 세션 정리 태스크릿은 삭제 건수를 기록하고 FINISHED를 반환한다")
  void jwtSessionCleanupTasklet_삭제하고_FINISHED를_반환한다() {
    given(jwtSessionRepository.deleteAllRevokedOrExpired(any(Instant.class))).willReturn(5);
    JwtSessionCleanupTasklet tasklet = new JwtSessionCleanupTasklet(jwtSessionRepository);
    StepContribution contribution = contribution();

    RepeatStatus status = tasklet.execute(contribution, chunkContext());

    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    assertThat(contribution.getWriteCount()).isEqualTo(5);
    then(jwtSessionRepository).should().deleteAllRevokedOrExpired(any(Instant.class));
  }

  @Test
  @DisplayName("임시 비밀번호 토큰 정리 태스크릿은 삭제 건수를 기록하고 FINISHED를 반환한다")
  void passwordResetTokenCleanupTasklet_삭제하고_FINISHED를_반환한다() {
    given(passwordResetTokenRepository.deleteAllUsedOrExpired(any(Instant.class))).willReturn(3);
    PasswordResetTokenCleanupTasklet tasklet =
        new PasswordResetTokenCleanupTasklet(passwordResetTokenRepository);
    StepContribution contribution = contribution();

    RepeatStatus status = tasklet.execute(contribution, chunkContext());

    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    assertThat(contribution.getWriteCount()).isEqualTo(3);
    then(passwordResetTokenRepository).should().deleteAllUsedOrExpired(any(Instant.class));
  }

  @Test
  @DisplayName("시청 세션 정리 태스크릿은 종료 건수를 기록하고 FINISHED를 반환한다")
  void watchingSessionCleanupTasklet_종료하고_FINISHED를_반환한다() {
    given(watchingSessionRepository.endAllActiveStartedBefore(any(Instant.class), any(Instant.class)))
        .willReturn(7);
    WatchingSessionCleanupTasklet tasklet =
        new WatchingSessionCleanupTasklet(watchingSessionRepository);
    StepContribution contribution = contribution();

    RepeatStatus status = tasklet.execute(contribution, chunkContext());

    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    assertThat(contribution.getWriteCount()).isEqualTo(7);
    then(watchingSessionRepository).should()
        .endAllActiveStartedBefore(any(Instant.class), any(Instant.class));
  }
}
