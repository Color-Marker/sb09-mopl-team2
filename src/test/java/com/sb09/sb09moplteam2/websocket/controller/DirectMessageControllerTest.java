package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.exception.ErrorResponse;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.dto.request.DirectMessageRequest;
import com.sb09.sb09moplteam2.websocket.service.DirectMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sb09.sb09moplteam2.websocket.relay.StompBroadcastRelay;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DirectMessageControllerTest {

  @Mock
  private DirectMessageService directMessageService;
  @Mock
  private StompBroadcastRelay stompBroadcastRelay;

  @InjectMocks
  private DirectMessageController directMessageController;

  private UUID conversationId;
  private UUID senderId;
  private UUID receiverId;

  @BeforeEach
  void setUp() {
    conversationId = UUID.randomUUID();
    senderId = UUID.randomUUID();
    receiverId = UUID.randomUUID();
  }

  // ───────────────────────────── sendDirectMessage ─────────────────────────────

  @Test
  void sendDirectMessage_정상_전송시_해당_대화방_토픽으로_브로드캐스트한다() {
    DirectMessageRequest request = new DirectMessageRequest("안녕하세요");
    DirectMessageDto response = new DirectMessageDto(
        UUID.randomUUID(),
        conversationId,
        Instant.now(),
        new UserSummary(senderId, "sender", null),
        new UserSummary(receiverId, "receiver", null),
        "안녕하세요"
    );

    given(directMessageService.send(conversationId, senderId, "안녕하세요"))
        .willReturn(response);

    directMessageController.sendDirectMessage(conversationId, request, senderId);

    verify(stompBroadcastRelay).broadcast(
        eq("/sub/conversations/" + conversationId + "/direct-messages"), eq(response));
  }

  @Test
  void sendDirectMessage_서비스에_conversationId_senderId_content를_그대로_전달한다() {
    String content = "테스트 메시지";
    DirectMessageRequest request = new DirectMessageRequest(content);
    DirectMessageDto response = new DirectMessageDto(
        UUID.randomUUID(),
        conversationId,
        Instant.now(),
        new UserSummary(senderId, "sender", null),
        new UserSummary(receiverId, "receiver", null),
        content
    );

    given(directMessageService.send(conversationId, senderId, content))
        .willReturn(response);

    directMessageController.sendDirectMessage(conversationId, request, senderId);

    verify(directMessageService).send(conversationId, senderId, content);
  }

  // ───────────────────────────── handleMoplException ─────────────────────────────

  @Test
  void handleMoplException_MoplException을_ErrorResponse로_변환한다() {
    ConversationNotFoundException exception = new ConversationNotFoundException(conversationId);

    ErrorResponse result = directMessageController.handleMoplException(exception);

    assertThat(result.getMessage()).isEqualTo(exception.getMessage());
    assertThat(result.getExceptionName()).isEqualTo("ConversationNotFoundException");
    assertThat(result.getDetails()).containsEntry("id", conversationId);
  }

  // ───────────────────────────── handleException ─────────────────────────────

  @Test
  void handleException_일반_예외를_ErrorResponse로_변환한다() {
    RuntimeException exception = new RuntimeException("예상치 못한 오류");

    ErrorResponse result = directMessageController.handleException(exception);

    assertThat(result.getMessage()).isEqualTo("예상치 못한 오류");
    assertThat(result.getExceptionName()).isEqualTo("RuntimeException");
  }
}