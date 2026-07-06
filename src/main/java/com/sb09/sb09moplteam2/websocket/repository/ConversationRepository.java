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

  @Query("""
        SELECT c FROM Conversation c
        JOIN ConversationParticipant cp ON cp.conversation = c
        WHERE cp.userId = :userId
        ORDER BY c.lastMessageAt DESC, c.id DESC
        """)
  List<Conversation> findAllByParticipantUserId(
      @Param("userId") UUID userId, Pageable pageable);

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

  // 키워드 검색용 - 커서 없이 전체 목록 (애플리케이션 레벨 필터링/페이징에 사용)
  @Query("""
        SELECT c FROM Conversation c
        JOIN ConversationParticipant cp ON cp.conversation = c
        WHERE cp.userId = :userId
        ORDER BY c.lastMessageAt DESC, c.id DESC
        """)
  List<Conversation> findAllByParticipantUserIdNoPaging(@Param("userId") UUID userId);
}
