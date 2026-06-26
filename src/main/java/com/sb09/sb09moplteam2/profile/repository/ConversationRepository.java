package com.sb09.sb09moplteam2.profile.repository;

import com.sb09.sb09moplteam2.profile.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  Optional<Conversation> findByUser1IdAndUser2Id(UUID user1Id, UUID user2Id);

  @Query("SELECT c FROM Conversation c " +
      "WHERE (c.user1.id = :userId OR c.user2.id = :userId) " +
      "AND c.id < :cursor " +
      "ORDER BY c.id DESC")
  Slice<Conversation> findMyConversationsWithCursor(
      @Param("userId") UUID userId,
      @Param("cursor") UUID cursor,
      Pageable pageable
  );
}
