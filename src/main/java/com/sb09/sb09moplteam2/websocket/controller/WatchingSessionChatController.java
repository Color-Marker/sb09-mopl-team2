package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.exception.ErrorResponse;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.websocket.dto.request.WatchingSessionChatRequest;
import com.sb09.sb09moplteam2.websocket.dto.response.WatchingSessionChatResponse;
import com.sb09.sb09moplteam2.websocket.relay.StompBroadcastRelay;
import com.sb09.sb09moplteam2.websocket.service.WatchingSessionChatService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

/**
 * 컨텐츠 같이보기 실시간 채팅
 *
 * 클라이언트 전송 경로:   /pub/contents/{contentId}/chat
 * 브로드캐스트 경로:      /sub/contents/{contentId}/chat
 *
 * 메시지는 DB에 저장하지 않고 접속 중인 구독자에게만 실시간으로 전달합니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WatchingSessionChatController {

  private final WatchingSessionChatService watchingSessionChatService;
  private final StompBroadcastRelay stompBroadcastRelay;

  @MessageMapping("/contents/{contentId}/chat")
  public void sendMessage(
      @DestinationVariable UUID contentId,
      @Payload @Valid WatchingSessionChatRequest request,
      @AuthenticationPrincipal UUID senderId
  ) {
    log.debug("STOMP 컨텐츠 채팅 수신: contentId={}, senderId={}", contentId, senderId);

    WatchingSessionChatResponse response = watchingSessionChatService.sendMessage(
        contentId, senderId, request.content());

    // 다중 인스턴스 브로드캐스트: Redis 경유로 모든 인스턴스의 구독자에게 전달
    stompBroadcastRelay.broadcast("/sub/contents/" + contentId + "/chat", response);
  }

  @MessageExceptionHandler(MoplException.class)
  @SendToUser("/queue/errors")
  public ErrorResponse handleMoplException(MoplException e) {
    log.warn("STOMP 컨텐츠 채팅 처리 실패 (도메인 예외): {}", e.getMessage());
    return new ErrorResponse(e);
  }

  @MessageExceptionHandler(Exception.class)
  @SendToUser("/queue/errors")
  public ErrorResponse handleException(Exception e) {
    log.error("STOMP 컨텐츠 채팅 처리 실패 (예상치 못한 예외)", e);
    return new ErrorResponse(e);
  }
}
