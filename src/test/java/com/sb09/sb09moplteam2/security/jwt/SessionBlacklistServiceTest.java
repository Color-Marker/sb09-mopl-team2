package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.config.jwt.JwtProperties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class SessionBlacklistServiceTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ValueOperations<String, Object> valueOperations;
  @Mock private JwtProperties jwtProperties;
  @Mock private JwtProperties.Token accessTokenProperties;

  private SessionBlacklistService sessionBlacklistService;

  @BeforeEach
  void setUp() {
    sessionBlacklistService = new SessionBlacklistService(redisTemplate, jwtProperties);
  }

  @Test
  void blacklist_호출_시_Redis에_세션ID_키로_저장한다() {
    UUID sessionId = UUID.randomUUID();
    given(jwtProperties.getAccessToken()).willReturn(accessTokenProperties);
    given(accessTokenProperties.getExpirationMs()).willReturn(3_600_000L);
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    sessionBlacklistService.blacklist(sessionId);

    verify(valueOperations).set(
        "blacklist:session:" + sessionId,
        "1",
        3600L,
        TimeUnit.SECONDS
    );
  }

  @Test
  void isBlacklisted_키가_존재하면_true를_반환한다() {
    UUID sessionId = UUID.randomUUID();
    given(redisTemplate.hasKey("blacklist:session:" + sessionId)).willReturn(true);

    assertThat(sessionBlacklistService.isBlacklisted(sessionId)).isTrue();
  }

  @Test
  void isBlacklisted_키가_없으면_false를_반환한다() {
    UUID sessionId = UUID.randomUUID();
    given(redisTemplate.hasKey("blacklist:session:" + sessionId)).willReturn(false);

    assertThat(sessionBlacklistService.isBlacklisted(sessionId)).isFalse();
  }

  @Test
  void isBlacklisted_Redis가_null을_반환하면_false를_반환한다() {
    UUID sessionId = UUID.randomUUID();
    given(redisTemplate.hasKey("blacklist:session:" + sessionId)).willReturn(null);

    assertThat(sessionBlacklistService.isBlacklisted(sessionId)).isFalse();
  }
}