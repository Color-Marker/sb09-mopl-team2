package com.sb09.sb09moplteam2.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.sse.SseMessage;
import com.sb09.sb09moplteam2.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

  private final ObjectMapper objectMapper;
  private final SseService sseService;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      SseMessage payload = objectMapper.readValue(
          message.getBody(), SseMessage.class);
      sseService.send(payload);
    } catch (Exception e) {
      log.error("Redis SSE 메시지 처리 실패", e);
    }
  }
}