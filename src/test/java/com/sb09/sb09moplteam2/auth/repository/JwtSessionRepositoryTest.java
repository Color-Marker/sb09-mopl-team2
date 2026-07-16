package com.sb09.sb09moplteam2.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.config.JpaAuditingConfig;
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
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
class JwtSessionRepositoryTest {

  @Autowired
  private JwtSessionRepository jwtSessionRepository;

  @Autowired
  private TestEntityManager em;

  private JwtSession persistSession(String refreshToken, Instant expirationTime, boolean revoked) {
    JwtSession session = new JwtSession(UUID.randomUUID(), refreshToken, expirationTime);
    if (revoked) {
      session.revoke();
    }
    em.persist(session);
    return session;
  }

  @Test
  @DisplayName("revoke된 세션과 만료된 세션만 삭제하고 활성 세션은 남긴다")
  void deleteAllRevokedOrExpired_폐기_만료_세션만_삭제한다() {
    JwtSession active = persistSession("active-token", Instant.now().plusSeconds(3600), false);
    persistSession("revoked-token", Instant.now().plusSeconds(3600), true);
    persistSession("expired-token", Instant.now().minusSeconds(3600), false);
    persistSession("revoked-expired-token", Instant.now().minusSeconds(3600), true);
    em.flush();
    em.clear();

    int deleted = jwtSessionRepository.deleteAllRevokedOrExpired(Instant.now());

    assertThat(deleted).isEqualTo(3);
    assertThat(jwtSessionRepository.findAll())
        .extracting(JwtSession::getId)
        .containsExactly(active.getId());
  }

  @Test
  @DisplayName("삭제 대상이 없으면 0을 반환한다")
  void deleteAllRevokedOrExpired_대상없으면_0을_반환한다() {
    persistSession("active-token", Instant.now().plusSeconds(3600), false);
    em.flush();
    em.clear();

    int deleted = jwtSessionRepository.deleteAllRevokedOrExpired(Instant.now());

    assertThat(deleted).isZero();
    assertThat(jwtSessionRepository.count()).isEqualTo(1);
  }
}
