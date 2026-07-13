package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.websocket.dto.response.WatchingSessionChatResponse;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import com.sb09.sb09moplteam2.websocket.mapper.WatchingSessionChatMapper;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchingSessionChatService {

  private final WatchingSessionRepository watchingSessionRepository;
  private final UserRepository userRepository;
  private final WatchingSessionChatMapper watchingSessionChatMapper;

  public WatchingSessionChatResponse sendMessage(UUID contentId, UUID senderId, String content) {
    WatchingSession session = watchingSessionRepository
        .findByUserIdAndStatus(senderId, WatchingSessionStatus.ACTIVE)
        .orElseThrow(() -> new MoplException(ErrorCode.WATCHING_SESSION_NOT_FOUND));

    if (!session.getContentId().equals(contentId)) {
      throw new MoplException(ErrorCode.WATCHING_SESSION_CONTENT_MISMATCH);
    }

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new MoplException(ErrorCode.USER_NOT_FOUND));

    return watchingSessionChatMapper.toResponse(sender, content);
  }
}
