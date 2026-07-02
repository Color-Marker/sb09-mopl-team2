package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.websocket.dto.request.DirectMessageRequest;
import com.sb09.sb09moplteam2.websocket.dto.response.DirectMessageResponse;
import com.sb09.sb09moplteam2.websocket.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageController {

  private final DirectMessageService directMessageService;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * DM 전송
   *
   * 클라이언트 전송 경로: /app/dm/{conversationId}
   * 브로드캐스트 경로:    /topic/conversations/{conversationId}
   *
   * 클라이언트는 CONNECT 후 /topic/conversations/{conversationId} 를 구독하고,
   * 메시지를 보낼 때는 /app/dm/{conversationId} 로 전송합니다.
   */
  @MessageMapping("/dm/{conversationId}")
  public void sendDirectMessage(
      @DestinationVariable UUID conversationId,
      @Payload @Valid DirectMessageRequest request,
      @AuthenticationPrincipal UUID senderId
  ) {
    log.debug("STOMP DM 수신: conversationId={}, senderId={}", conversationId, senderId);

    DirectMessageResponse response = directMessageService.send(
        conversationId, senderId, request.content());

    // 대화방 구독자 전체에게 브로드캐스트
    messagingTemplate.convertAndSend(
        "/topic/conversations/" + conversationId,
        response
    );
  }
}
