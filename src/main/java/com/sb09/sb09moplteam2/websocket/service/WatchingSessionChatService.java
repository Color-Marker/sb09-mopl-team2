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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        .orElseThrow(() -> {
          log.warn("채팅 전송 실패 - 활성 시청 세션 없음: senderId={}", senderId);
          return new MoplException(ErrorCode.WATCHING_SESSION_NOT_FOUND);
        });

    if (!session.getContentId().equals(contentId)) {
      log.warn("채팅 전송 실패 - 컨텐츠 불일치: senderId={}, expectedContentId={}, actualContentId={}",
          senderId, contentId, session.getContentId());
      throw new MoplException(ErrorCode.WATCHING_SESSION_CONTENT_MISMATCH);
    }

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> {
          log.warn("채팅 전송 실패 - 유저 없음: senderId={}", senderId);
          return new MoplException(ErrorCode.USER_NOT_FOUND);
        });

    return watchingSessionChatMapper.toResponse(sender, content);
  }
}
