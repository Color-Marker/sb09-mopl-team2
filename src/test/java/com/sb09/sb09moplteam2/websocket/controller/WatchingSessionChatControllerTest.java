package com.sb09.sb09moplteam2.websocket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.ErrorResponse;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.websocket.dto.request.WatchingSessionChatRequest;
import com.sb09.sb09moplteam2.websocket.dto.response.WatchingSessionChatResponse;
import com.sb09.sb09moplteam2.websocket.service.WatchingSessionChatService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class WatchingSessionChatControllerTest {

  @Mock
  private WatchingSessionChatService watchingSessionChatService;
  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private WatchingSessionChatController watchingSessionChatController;

  private UUID contentId;
  private UUID senderId;
  private WatchingSessionChatRequest request;

  @BeforeEach
  void setUp() {
    contentId = UUID.randomUUID();
    senderId = UUID.randomUUID();
    request = new WatchingSessionChatRequest("안녕하세요");
  }

  @Test
  void 메시지_전송에_성공하면_해당_콘텐츠_채팅_destination으로_브로드캐스트한다() {
    WatchingSessionChatResponse response = new WatchingSessionChatResponse(
        senderId, "테스트유저", "https://example.com/profile.jpg", request.content(), Instant.now());

    given(watchingSessionChatService.sendMessage(contentId, senderId, request.content()))
        .willReturn(response);

    watchingSessionChatController.sendMessage(contentId, request, senderId);

    ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/sub/contents/" + contentId + "/chat"), payloadCaptor.capture());
    assertThat(payloadCaptor.getValue()).isEqualTo(response);
  }

  @Test
  void 서비스에서_MoplException이_발생하면_그대로_전파된다() {
    MoplException exception = new MoplException(ErrorCode.WATCHING_SESSION_NOT_FOUND);

    given(watchingSessionChatService.sendMessage(contentId, senderId, request.content()))
        .willThrow(exception);

    // @MessageExceptionHandler로의 자동 라우팅은 STOMP 메시징 인프라가 처리하므로
    // 단위 테스트에서는 컨트롤러가 예외를 그대로 던지는지만 검증
    assertThatThrownBy(() ->
        watchingSessionChatController.sendMessage(contentId, request, senderId))
        .isInstanceOf(MoplException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.WATCHING_SESSION_NOT_FOUND);
  }

  @Test
  void handleMoplException은_ErrorResponse를_생성한다() {
    MoplException exception = new MoplException(ErrorCode.WATCHING_SESSION_CONTENT_MISMATCH);

    ErrorResponse errorResponse = watchingSessionChatController.handleMoplException(exception);

    assertThat(errorResponse).isNotNull();
  }

  @Test
  void handleException은_일반_예외도_ErrorResponse로_변환한다() {
    Exception exception = new RuntimeException("예상치 못한 오류");

    ErrorResponse errorResponse = watchingSessionChatController.handleException(exception);

    assertThat(errorResponse).isNotNull();
  }
}
