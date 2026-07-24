package com.sb09.sb09moplteam2.websocket.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.service.ContentService;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WatchingSessionMapperTest {

  @Mock
  private UserService userService;
  @Mock
  private ContentService contentService;

  @InjectMocks
  private WatchingSessionMapper watchingSessionMapper;

  private UUID sessionId;
  private UUID userId;
  private UUID contentId;
  private WatchingSession session;

  @BeforeEach
  void setUp() {
    sessionId = UUID.randomUUID();
    userId = UUID.randomUUID();
    contentId = UUID.randomUUID();

    session = WatchingSession.create(userId, contentId);
    ReflectionTestUtils.setField(session, "id", sessionId);
  }

  @Test
  void watcher와_content_정보를_포함한_WatchingSessionDto를_생성한다() {
    UserSummary watcherSummary = new UserSummary(userId, "시청자", "profile.jpg");
    ContentSummary contentSummary = new ContentSummary(
        contentId, ContentType.movie, "테스트 컨텐츠", "설명", "thumb.jpg",
        List.of("액션"), 4.5, 10, 1);

    given(userService.getUserSummary(userId)).willReturn(watcherSummary);
    given(contentService.getContentSummary(contentId)).willReturn(contentSummary);

    WatchingSessionDto result = watchingSessionMapper.toDto(session);

    assertThat(result.id()).isEqualTo(sessionId);
    assertThat(result.watcher()).isEqualTo(watcherSummary);
    assertThat(result.content()).isEqualTo(contentSummary);
    assertThat(result.createdAt()).isEqualTo(session.getStartedAt());
  }
}
