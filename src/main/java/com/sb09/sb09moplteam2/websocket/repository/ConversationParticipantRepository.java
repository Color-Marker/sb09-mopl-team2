package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {

  List<ConversationParticipant> findByConversation(Conversation conversation);

  boolean existsByConversationAndUserId(Conversation conversation, UUID userId);

  Optional<ConversationParticipant> findByConversationAndUserId(Conversation conversation, UUID userId);

  @Query("""
        SELECT cp.conversation FROM ConversationParticipant cp
        WHERE cp.userId IN (:userIdA, :userIdB)
          AND cp.conversation.type = com.sb09.sb09moplteam2.websocket.entity.ConversationType.DIRECT
        GROUP BY cp.conversation
        HAVING COUNT(cp) = 2
        """)
  Optional<Conversation> findExistingDirectConversation(
      @Param("userIdA") UUID userIdA,
      @Param("userIdB") UUID userIdB
  );

  // N+1 방지용 IN 쿼리 추가
  @Query("""
        SELECT cp FROM ConversationParticipant cp
        WHERE cp.conversation.id IN :conversationIds
        """)
  List<ConversationParticipant> findByConversationIdIn(
      @Param("conversationIds") List<UUID> conversationIds
  );
}
