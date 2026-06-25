package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import com.sb09.sb09moplteam2.exception.websocket.WatchingSessionNotFoundException;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchingSessionService {

  private final WatchingSessionRepository watchingSessionRepository;

  // 세션 생성
  @Transactional
  public WatchingSession create(UUID userId, UUID contentId) {
    // 이미 활성 세션이 있으면 중복 생성 방지
    if (watchingSessionRepository.existsByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE)) {
      throw new IllegalStateException("이미 활성 상태의 시청 세션이 존재합니다. userId=" + userId);
    }

    WatchingSession session = WatchingSession.create(userId, contentId);
    return watchingSessionRepository.save(session);
  }

  // 단건 조회
  public WatchingSession findById(UUID id) {
    return watchingSessionRepository.findById(id)
        .orElseThrow(() -> new WatchingSessionNotFoundException(id));
  }

  // 유저별 세션 목록 조회
  public List<WatchingSession> findAllByUserId(UUID userId) {
    return watchingSessionRepository.findByUserId(userId);
  }

  // 콘텐츠별 세션 목록 조회
  public List<WatchingSession> findAllByContentId(UUID contentId) {
    return watchingSessionRepository.findByContentId(contentId);
  }

  // 세션 종료
  @Transactional
  public WatchingSession end(UUID id) {
    WatchingSession session = watchingSessionRepository.findById(id)
        .orElseThrow(() -> new WatchingSessionNotFoundException(id));

    session.end();
    return session; // @Transactional 범위 안이라 dirty checking으로 자동 반영
  }
}
