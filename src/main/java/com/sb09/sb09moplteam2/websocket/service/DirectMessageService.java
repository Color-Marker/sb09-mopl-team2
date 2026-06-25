package com.sb09.sb09moplteam2.websocket.service;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.DirectMessageNotFoundException;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;

  // 메시지 전송
  @Transactional
  public DirectMessage send(UUID conversationId, UUID senderId, String content) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    // 참여자가 아닌 유저는 메시지 전송 불가
    if (!conversationParticipantRepository.existsByConversationAndUserId(conversation, senderId)) {
      throw new ConversationParticipantNotFoundException(conversationId, senderId);
    }

    DirectMessage message = DirectMessage.of(conversation, senderId, content);
    return directMessageRepository.save(message);
  }

  // 단건 조회
  public DirectMessage findById(UUID id) {
    return directMessageRepository.findById(id)
        .orElseThrow(() -> new DirectMessageNotFoundException(id));
  }

  // 대화방 메시지 목록 조회 (무한스크롤)
  public Slice<DirectMessage> findAllByConversationId(UUID conversationId, int page, int size) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    return directMessageRepository.findByConversationOrderBySentAtDesc(
        conversation, PageRequest.of(page, size));
  }
}
