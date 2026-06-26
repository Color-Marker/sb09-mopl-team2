package com.sb09.sb09moplteam2.profile.repository;

import com.sb09.sb09moplteam2.profile.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

  Slice<DirectMessage> findByConversationIdAndIdLessThanOrderByIdDesc(
      UUID conversationId,
      UUID cursor,
      Pageable pageable
  );

  boolean existsByConversationIdAndReceiverIdAndIsReadFalse(
      UUID conversationId,
      UUID receiverId
  );
}
