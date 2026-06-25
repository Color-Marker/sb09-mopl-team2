package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {

  // 대화방의 참여자 목록
  List<ConversationParticipant> findByConversation(Conversation conversation);

  // 특정 유저가 해당 대화방에 이미 참여 중인지 확인 (중복 참여 방지)
  boolean existsByConversationAndUserId(Conversation conversation, UUID userId);

  // 특정 유저 + 대화방으로 참여자 단건 조회 (lastReadAt 업데이트용)
  Optional<ConversationParticipant> findByConversationAndUserId(Conversation conversation, UUID userId);

  // DIRECT 대화방에서 두 유저가 이미 1:1 채팅 중인지 확인
  // → 두 유저 모두 참여한 DIRECT conversation 찾기
  @org.springframework.data.jpa.repository.Query("""
        SELECT cp.conversation FROM ConversationParticipant cp
        WHERE cp.userId IN (:userIdA, :userIdB)
          AND cp.conversation.type = com.sb09.sb09moplteam2.websocket.entity.ConversationType.DIRECT
        GROUP BY cp.conversation
        HAVING COUNT(cp) = 2
        """)
  java.util.Optional<Conversation> findExistingDirectConversation(
      @org.springframework.data.repository.query.Param("userIdA") UUID userIdA,
      @org.springframework.data.repository.query.Param("userIdB") UUID userIdB
  );
}
