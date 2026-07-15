package com.sb09.sb09moplteam2.websocket.mapper;

import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.websocket.dto.response.WatchingSessionChatResponse;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class WatchingSessionChatMapper {

  public WatchingSessionChatResponse toResponse(User sender, String content) {
    return new WatchingSessionChatResponse(
        sender.getId(),
        sender.getName(),
        content,
        Instant.now()
    );
  }
}
