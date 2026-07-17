package com.sb09.sb09moplteam2.websocket.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.websocket.dto.response.WatchingSessionChatResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WatchingSessionChatMapperTest {

  private WatchingSessionChatMapper watchingSessionChatMapper;
  private User sender;
  private UUID senderId;

  @BeforeEach
  void setUp() {
    watchingSessionChatMapper = new WatchingSessionChatMapper();
    sender = new User("테스트유저", "test@test.com", "password");
    senderId = UUID.randomUUID();
    ReflectionTestUtils.setField(sender, "id", senderId);
    ReflectionTestUtils.setField(sender, "profileImageUrl", "profile.jpg");
  }

  @Test
  void sender_정보와_content로_response를_생성한다() {
    String content = "안녕하세요";

    WatchingSessionChatResponse result = watchingSessionChatMapper.toResponse(sender, content);

    assertThat(result.senderId()).isEqualTo(senderId);
    assertThat(result.senderName()).isEqualTo("테스트유저");
    assertThat(result.senderProfileImageUrl()).isEqualTo("profile.jpg");
    assertThat(result.content()).isEqualTo(content);
    assertThat(result.sentAt()).isNotNull();
  }

  @Test
  void profileImageUrl이_null인_유저도_정상_처리된다() {
    User senderWithoutImage = new User("이미지없는유저", "noimg@test.com", "password");
    ReflectionTestUtils.setField(senderWithoutImage, "id", senderId);

    WatchingSessionChatResponse result =
        watchingSessionChatMapper.toResponse(senderWithoutImage, "테스트");

    assertThat(result.senderProfileImageUrl()).isNull();
  }
}
