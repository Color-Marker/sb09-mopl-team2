package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.websocket.dto.response.WatchingSessionChatResponse;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import com.sb09.sb09moplteam2.websocket.mapper.WatchingSessionChatMapper;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WatchingSessionChatServiceTest {

  @Mock
  private WatchingSessionRepository watchingSessionRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private WatchingSessionChatMapper watchingSessionChatMapper;

  @InjectMocks
  private WatchingSessionChatService watchingSessionChatService;

  private UUID senderId;
  private UUID contentId;
  private WatchingSession activeSession;
  private User sender;

  @BeforeEach
  void setUp() {
    senderId = UUID.randomUUID();
    contentId = UUID.randomUUID();

    activeSession = WatchingSession.create(senderId, contentId);
    ReflectionTestUtils.setField(activeSession, "id", UUID.randomUUID());

    sender = mock(User.class);
  }

  @Test
  void 활성_세션과_일치하는_콘텐츠면_메시지를_정상_전송한다() {
    String content = "안녕하세요";

    WatchingSessionChatResponse expectedResponse =
        new WatchingSessionChatResponse(
            new WatchingSessionChatResponse.Sender(
                senderId,
                "테스트유저",
                "https://example.com/profile.jpg"
            ),
            content,
            Instant.now()
        );

    given(watchingSessionRepository.findByUserIdAndStatus(
        senderId,
        WatchingSessionStatus.ACTIVE
    )).willReturn(Optional.of(activeSession));

    given(userRepository.findById(senderId))
        .willReturn(Optional.of(sender));

    given(watchingSessionChatMapper.toResponse(sender, content))
        .willReturn(expectedResponse);

    WatchingSessionChatResponse result =
        watchingSessionChatService.sendMessage(contentId, senderId, content);

    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void 활성_세션이_없으면_WATCHING_SESSION_NOT_FOUND_예외를_던진다() {
    given(watchingSessionRepository.findByUserIdAndStatus(senderId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.empty());

    assertThatThrownBy(() ->
        watchingSessionChatService.sendMessage(contentId, senderId, "hi"))
        .isInstanceOf(MoplException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.WATCHING_SESSION_NOT_FOUND);

    verify(userRepository, never()).findById(any());
  }

  @Test
  void 세션의_콘텐츠와_요청_콘텐츠가_다르면_WATCHING_SESSION_CONTENT_MISMATCH_예외를_던진다() {
    UUID otherContentId = UUID.randomUUID();

    given(watchingSessionRepository.findByUserIdAndStatus(senderId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.of(activeSession)); // activeSession.contentId != otherContentId

    assertThatThrownBy(() ->
        watchingSessionChatService.sendMessage(otherContentId, senderId, "hi"))
        .isInstanceOf(MoplException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.WATCHING_SESSION_CONTENT_MISMATCH);

    verify(userRepository, never()).findById(any());
  }

  @Test
  void 발신자_유저가_존재하지_않으면_USER_NOT_FOUND_예외를_던진다() {
    given(watchingSessionRepository.findByUserIdAndStatus(senderId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.of(activeSession));
    given(userRepository.findById(senderId)).willReturn(Optional.empty());

    assertThatThrownBy(() ->
        watchingSessionChatService.sendMessage(contentId, senderId, "hi"))
        .isInstanceOf(MoplException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }
}
