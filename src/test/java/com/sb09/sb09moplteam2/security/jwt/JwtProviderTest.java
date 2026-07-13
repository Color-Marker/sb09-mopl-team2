package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.config.jwt.JwtProperties;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final String ACCESS_SECRET = "test-access-secret-key-must-be-long-enough-1234567890";
  private static final String REFRESH_SECRET = "test-refresh-secret-key-must-be-long-enough-1234567890";

  private JwtProvider jwtProvider;
  private JwtProperties jwtProperties;

  @BeforeEach
  void setUp() {
    jwtProperties = new JwtProperties();
    jwtProperties.getAccessToken().setSecret(ACCESS_SECRET);
    jwtProperties.getAccessToken().setExpirationMs(60_000);
    jwtProperties.getRefreshToken().setSecret(REFRESH_SECRET);
    jwtProperties.getRefreshToken().setExpirationMs(604_800_000);

    jwtProvider = new JwtProvider(jwtProperties);
  }

  @Test
  void 액세스_토큰을_발급하고_검증하면_userId와_role을_그대로_꺼낼_수_있다() {
    UUID userId = UUID.randomUUID();
    String token = jwtProvider.generateAccessToken(userId, Role.USER, UUID.randomUUID());

    assertThat(jwtProvider.isValid(token)).isTrue();
    assertThat(jwtProvider.getUserId(token)).isEqualTo(userId);
    assertThat(jwtProvider.getRole(token)).isEqualTo(Role.USER);
  }

  @Test
  void 리프레시_토큰을_발급하고_검증하면_userId를_그대로_꺼낼_수_있다() {
    UUID userId = UUID.randomUUID();
    String refreshToken = jwtProvider.generateRefreshToken(userId);

    assertThat(jwtProvider.isValidRefreshToken(refreshToken)).isTrue();
    assertThat(jwtProvider.getUserIdFromRefreshToken(refreshToken)).isEqualTo(userId);
  }

  @Test
  void 만료시간이_지난_토큰은_유효하지_않다() {
    jwtProperties.getAccessToken().setExpirationMs(-1000); // 발급 즉시 과거로 만료
    String expiredToken = jwtProvider.generateAccessToken(UUID.randomUUID(), Role.USER, UUID.randomUUID());

    assertThat(jwtProvider.isValid(expiredToken)).isFalse();
  }

  @Test
  void 형식이_올바르지_않은_토큰은_유효하지_않다() {
    assertThat(jwtProvider.isValid("this-is-not-a-jwt")).isFalse();
  }

  @Test
  void 다른_시크릿으로_서명된_토큰은_검증에_실패한다() {
    UUID userId = UUID.randomUUID();
    String token = jwtProvider.generateAccessToken(userId, Role.USER, UUID.randomUUID());

    JwtProperties otherProperties = new JwtProperties();
    otherProperties.getAccessToken().setSecret("completely-different-secret-key-1234567890-xyz");
    otherProperties.getAccessToken().setExpirationMs(60_000);
    otherProperties.getRefreshToken().setSecret(REFRESH_SECRET);
    otherProperties.getRefreshToken().setExpirationMs(604_800_000);
    JwtProvider otherProvider = new JwtProvider(otherProperties);

    assertThat(otherProvider.isValid(token)).isFalse();
  }
}