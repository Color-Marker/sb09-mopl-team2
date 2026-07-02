package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  // 특정 유저가 참여한 대화방 목록 - 커서 없을 때 (첫 페이지)
  // lastMessageAt 기준 최신순
  @Query("""
        SELECT c FROM Conversation c
        JOIN ConversationParticipant cp ON cp.conversation = c
        WHERE cp.userId = :userId
        ORDER BY c.lastMessageAt DESC, c.id DESC
        """)
  List<Conversation> findAllByParticipantUserId(
      @Param("userId") UUID userId, Pageable pageable);

  // 특정 유저가 참여한 대화방 목록 - 커서 있을 때
  // (lastMessageAt 기준으로 그 이전 대화방 조회)
  @Query("""
        SELECT c FROM Conversation c
        JOIN ConversationParticipant cp ON cp.conversation = c
        WHERE cp.userId = :userId
          AND (c.lastMessageAt < :cursorLastMessageAt
            OR (c.lastMessageAt = :cursorLastMessageAt AND c.id < :idAfter))
        ORDER BY c.lastMessageAt DESC, c.id DESC
        """)
  List<Conversation> findAllByParticipantUserIdWithCursor(
      @Param("userId") UUID userId,
      @Param("cursorLastMessageAt") Instant cursorLastMessageAt,
      @Param("idAfter") UUID idAfter,
      Pageable pageable
  );
}
