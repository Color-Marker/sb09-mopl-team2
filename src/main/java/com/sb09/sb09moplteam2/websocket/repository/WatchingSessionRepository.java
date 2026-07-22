package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchingSessionRepository extends JpaRepository<WatchingSession, UUID> {

  // 유저의 ACTIVE 세션 단건 조회
  @Query("SELECT w FROM WatchingSession w WHERE w.userId = :userId AND w.status = 'ACTIVE'")
  Optional<WatchingSession> findByUserIdAndStatus(UUID userId, WatchingSessionStatus status);

  // 특정 유저의 세션 전체 조회
  List<WatchingSession> findByUserId(UUID userId);

  // 특정 콘텐츠의 세션 목록 - 커서 없을 때 (첫 페이지)
  // startedAt 기준 최신순. "현재 시청자 목록"이므로 종료(ENDED)된 세션은 제외
  @Query("""
        SELECT w FROM WatchingSession w
        WHERE w.contentId = :contentId
          AND w.status = 'ACTIVE'
        ORDER BY w.startedAt DESC, w.id DESC
        """)
  List<WatchingSession> findByContentId(
      @Param("contentId") UUID contentId, Pageable pageable);

  // 특정 콘텐츠의 세션 목록 - 커서 있을 때
  // (startedAt 기준으로 그 이전 세션 조회, ACTIVE만)
  @Query("""
        SELECT w FROM WatchingSession w
        WHERE w.contentId = :contentId
          AND w.status = 'ACTIVE'
          AND (w.startedAt < :cursorStartedAt
            OR (w.startedAt = :cursorStartedAt AND w.id < :idAfter))
        ORDER BY w.startedAt DESC, w.id DESC
        """)
  List<WatchingSession> findByContentIdWithCursor(
      @Param("contentId") UUID contentId,
      @Param("cursorStartedAt") Instant cursorStartedAt,
      @Param("idAfter") UUID idAfter,
      Pageable pageable
  );

  // 특정 유저의 활성 세션 조회 (중복 세션 방지용)
  boolean existsByUserIdAndStatus(UUID userId, WatchingSessionStatus status);

  // 오래된 ACTIVE 세션 일괄 종료 (연결 종료 이벤트 유실로 남은 유령 세션 정리용)
  @Modifying
  @Query("""
        UPDATE WatchingSession w
        SET w.status = com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus.ENDED,
            w.endedAt = :now
        WHERE w.status = com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus.ACTIVE
          AND w.startedAt < :threshold
        """)
  int endAllActiveStartedBefore(@Param("threshold") Instant threshold, @Param("now") Instant now);
}
