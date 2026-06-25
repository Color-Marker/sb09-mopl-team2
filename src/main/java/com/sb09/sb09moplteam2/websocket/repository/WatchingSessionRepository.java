package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WatchingSessionRepository extends JpaRepository<WatchingSession, UUID> {

  // 특정 유저의 세션 전체 조회
  List<WatchingSession> findByUserId(UUID userId);

  // 특정 콘텐츠의 세션 전체 조회
  List<WatchingSession> findByContentId(UUID contentId);

  // 특정 유저의 활성 세션 조회 (중복 세션 방지용)
  boolean existsByUserIdAndStatus(UUID userId, WatchingSessionStatus status);
}
