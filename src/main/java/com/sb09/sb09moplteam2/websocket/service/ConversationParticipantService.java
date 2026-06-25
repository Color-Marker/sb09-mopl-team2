package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationParticipantService {

  private final ConversationParticipantRepository conversationParticipantRepository;
  private final ConversationRepository conversationRepository;

  // 참여자 추가
  @Transactional
  public ConversationParticipant join(UUID conversationId, UUID userId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    // 이미 참여 중이면 중복 추가 방지
    if (conversationParticipantRepository.existsByConversationAndUserId(conversation, userId)) {
      throw new IllegalStateException("이미 참여 중인 대화방입니다. userId=" + userId);
    }

    ConversationParticipant participant = ConversationParticipant.of(conversation, userId);
    return conversationParticipantRepository.save(participant);
  }

  // 대화방 참여자 목록 조회
  public List<ConversationParticipant> findAllByConversationId(UUID conversationId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    return conversationParticipantRepository.findByConversation(conversation);
  }

  // 마지막 읽은 시각 업데이트 (메시지 읽음 처리)
  @Transactional
  public ConversationParticipant updateLastReadAt(UUID conversationId, UUID userId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    ConversationParticipant participant = conversationParticipantRepository
        .findByConversationAndUserId(conversation, userId)
        .orElseThrow(() -> new ConversationParticipantNotFoundException(conversationId, userId));

    participant.updateLastReadAt();
    return participant; // dirty checking으로 자동 반영
  }

  // 참여자 나가기
  @Transactional
  public void leave(UUID conversationId, UUID userId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    ConversationParticipant participant = conversationParticipantRepository
        .findByConversationAndUserId(conversation, userId)
        .orElseThrow(() -> new ConversationParticipantNotFoundException(conversationId, userId));

    conversationParticipantRepository.delete(participant);
  }
}
