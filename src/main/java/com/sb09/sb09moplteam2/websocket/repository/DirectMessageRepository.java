package com.sb09.sb09moplteam2.websocket.repository;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

  // 대화방의 메시지 목록 (무한스크롤 → Slice 사용)
  Slice<DirectMessage> findByConversationOrderBySentAtDesc(Conversation conversation, Pageable pageable);
}
