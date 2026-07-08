package com.sb09.sb09moplteam2.security.jwt;

import com.sb09.sb09moplteam2.config.jwt.JwtProperties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionBlacklistService {

  private static final String KEY_PREFIX = "blacklist:session:";

  private final RedisTemplate<String, Object> redisTemplate;
  private final JwtProperties jwtProperties;

  public void blacklist(UUID sessionId) {
    String key = KEY_PREFIX + sessionId;
    long ttlSeconds = jwtProperties.getAccessToken().getExpirationMs() / 1000;
    redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
  }

  public boolean isBlacklisted(UUID sessionId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + sessionId));
  }
}