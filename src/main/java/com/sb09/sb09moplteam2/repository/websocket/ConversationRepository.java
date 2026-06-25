package com.sb09.sb09moplteam2.repository.websocket;

import com.sb09.sb09moplteam2.entity.websocket.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  // 특정 유저가 참여한 대화방 목록 (ConversationParticipant 조인)
  @Query("""
        SELECT c FROM Conversation c
        JOIN ConversationParticipant cp ON cp.conversation = c
        WHERE cp.userId = :userId
        ORDER BY c.createdAt DESC
        """)
  List<Conversation> findAllByParticipantUserId(@Param("userId") UUID userId);
}
