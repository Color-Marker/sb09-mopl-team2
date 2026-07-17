package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.exception.ErrorResponse;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.dto.request.DirectMessageRequest;
import com.sb09.sb09moplteam2.websocket.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
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
   * 클라이언트 전송 경로: /app/dm/{conversationId}
   * 브로드캐스트 경로:    /topic/conversations/{conversationId}
   * 클라이언트는 CONNECT 후 /topic/conversations/{conversationId} 를 구독하고,
   * 메시지를 보낼 때는 /app/dm/{conversationId} 로 전송합니다.
   */
  @MessageMapping("/conversations/{conversationId}/direct-messages")
  public void sendDirectMessage(
      @DestinationVariable UUID conversationId,
      @Payload @Valid DirectMessageRequest request,
      @AuthenticationPrincipal UUID senderId
  ) {
    log.debug("STOMP DM 수신: conversationId={}, senderId={}", conversationId, senderId);

    DirectMessageDto response = directMessageService.send(
        conversationId, senderId, request.content());

    // 대화방 구독자 전체에게 브로드캐스트
    messagingTemplate.convertAndSend(
        "/sub/conversations/" + conversationId + "/direct-messages",
        response
    );
  }

  @MessageExceptionHandler(MoplException.class)
  @SendToUser("/queue/errors")
  public ErrorResponse handleMoplException(MoplException e) {
    log.warn("STOMP DM 처리 실패 (도메인 예외): {}", e.getMessage());
    return new ErrorResponse(e);
  }

  @MessageExceptionHandler(Exception.class)
  @SendToUser("/queue/errors")
  public ErrorResponse handleException(Exception e) {
    log.error("STOMP DM 처리 실패 (예상치 못한 예외)", e);
    return new ErrorResponse(e);
  }
}
