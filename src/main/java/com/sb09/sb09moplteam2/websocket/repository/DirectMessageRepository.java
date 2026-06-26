package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

  // 커서 없을 때 (첫 페이지)
  List<DirectMessage> findByConversationOrderBySentAtDesc(
      Conversation conversation, Pageable pageable);

  // 커서 있을 때 (sentAt 기준으로 그 이전 메시지 조회)
  @Query("""
        SELECT dm FROM DirectMessage dm
        WHERE dm.conversation = :conversation
          AND (dm.sentAt < :cursorSentAt
            OR (dm.sentAt = :cursorSentAt AND dm.id < :idAfter))
        ORDER BY dm.sentAt DESC, dm.id DESC
        """)
  List<DirectMessage> findByConversationWithCursor(
      @Param("conversation") Conversation conversation,
      @Param("cursorSentAt") Instant cursorSentAt,
      @Param("idAfter") UUID idAfter,
      Pageable pageable
  );
}
