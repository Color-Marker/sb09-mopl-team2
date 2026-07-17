package com.sb09.sb09moplteam2.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.config.JpaAuditingConfig;
import com.sb09.sb09moplteam2.config.MockSearchTestConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class, MockSearchTestConfig.class})
class PasswordResetTokenRepositoryTest {

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private TestEntityManager em;

  private PasswordResetToken persistToken(Instant expiryDate, boolean used) {
    PasswordResetToken token = new PasswordResetToken(UUID.randomUUID(), "encoded-temp", expiryDate);
    if (used) {
      token.markUsed();
    }
    em.persist(token);
    return token;
  }

  @Test
  @DisplayName("사용된 토큰과 만료된 토큰만 삭제하고 활성 토큰은 남긴다")
  void deleteAllUsedOrExpired_사용_만료_토큰만_삭제한다() {
    PasswordResetToken active = persistToken(Instant.now().plusSeconds(180), false);
    persistToken(Instant.now().plusSeconds(180), true);
    persistToken(Instant.now().minusSeconds(180), false);
    persistToken(Instant.now().minusSeconds(180), true);
    em.flush();
    em.clear();

    int deleted = passwordResetTokenRepository.deleteAllUsedOrExpired(Instant.now());

    assertThat(deleted).isEqualTo(3);
    assertThat(passwordResetTokenRepository.findAll())
        .extracting(PasswordResetToken::getId)
        .containsExactly(active.getId());
  }

  @Test
  @DisplayName("삭제 대상이 없으면 0을 반환한다")
  void deleteAllUsedOrExpired_대상없으면_0을_반환한다() {
    persistToken(Instant.now().plusSeconds(180), false);
    em.flush();
    em.clear();

    int deleted = passwordResetTokenRepository.deleteAllUsedOrExpired(Instant.now());

    assertThat(deleted).isZero();
    assertThat(passwordResetTokenRepository.count()).isEqualTo(1);
  }
}
