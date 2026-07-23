package com.sb09.sb09moplteam2.websocket.relay;

/**
 * 인스턴스 간 STOMP 브로드캐스트를 위해 Redis 채널로 전달되는 메시지.
 * payload는 GenericJackson2JsonRedisSerializer의 타입 정보 포함 직렬화로 원본 타입 그대로 복원된다.
 */
public record StompBroadcastMessage(
    String destination,
    Object payload
) {}
