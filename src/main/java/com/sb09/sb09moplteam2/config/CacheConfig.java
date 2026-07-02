package com.sb09.sb09moplteam2.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  // 캐시 이름 상수 — 서비스에서 @Cacheable(cacheNames = CacheConfig.WATCHING_SESSION) 으로 참조
  public static final String WATCHING_SESSION = "watchingSession";
  public static final String CONVERSATION = "conversation";

  @Bean
  public CacheManager cacheManager() {
    // 활성 세션 캐시: 실시간성이 중요하므로 TTL 30초
    CaffeineCache watchingSessionCache = new CaffeineCache(
        WATCHING_SESSION,
        Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build()
    );

    // 대화방 단건 캐시: 변경이 적으므로 TTL 5분
    CaffeineCache conversationCache = new CaffeineCache(
        CONVERSATION,
        Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(500)
            .build()
    );

    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(List.of(watchingSessionCache, conversationCache));
    return cacheManager;
  }
}
