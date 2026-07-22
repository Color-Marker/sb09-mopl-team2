package com.sb09.sb09moplteam2.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.sb09.sb09moplteam2.redis.RedisSubscriber;
import com.sb09.sb09moplteam2.websocket.relay.StompBroadcastSubscriber;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

  public static final String USER_CACHE = "users";

  public static final String SSE_CHANNEL = "sse-notification";
  public static final String STOMP_CHANNEL = "stomp-broadcast";

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
      @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);

    // Use String serializer for keys
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // Use JSON serializer for values
    template.setValueSerializer(redisSerializer);
    template.setHashValueSerializer(redisSerializer);

    template.afterPropertiesSet();

    return template;
  }

  @Bean("redisSerializer")
  public GenericJackson2JsonRedisSerializer redisSerializer(ObjectMapper objectMapper) {
    ObjectMapper redisObjectMapper = objectMapper.copy();
    redisObjectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        DefaultTyping.EVERYTHING,
        As.PROPERTY
    );
    return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
  }

  @Bean
  public RedisCacheManager cacheManager(
      RedisConnectionFactory connectionFactory,
      @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer) {
    // 기본 캐시 설정은 JDK 직렬화를 사용하므로 record DTO 캐싱을 위해 JSON 직렬화로 교체
    RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .disableCachingNullValues()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfig)
        .withCacheConfiguration(USER_CACHE, cacheConfig)
        .build();
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory connectionFactory,
      RedisSubscriber redisSubscriber,
      StompBroadcastSubscriber stompBroadcastSubscriber) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(redisSubscriber, ChannelTopic.of(SSE_CHANNEL));
    container.addMessageListener(stompBroadcastSubscriber, ChannelTopic.of(STOMP_CHANNEL));
    return container;
  }
}