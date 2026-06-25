package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
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
public class ConversationService {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;

  // 1:1 대화방 생성
  @Transactional
  public Conversation createDirect(UUID userIdA, UUID userIdB) {
    // 이미 두 유저 간 DIRECT 대화방이 있으면 재사용
    return conversationParticipantRepository
        .findExistingDirectConversation(userIdA, userIdB)
        .orElseGet(() -> {
          Conversation conversation = Conversation.createDirect();
          conversationRepository.save(conversation);

          conversationParticipantRepository.save(
              ConversationParticipant.of(conversation, userIdA));
          conversationParticipantRepository.save(
              ConversationParticipant.of(conversation, userIdB));

          return conversation;
        });
  }

  // 그룹 대화방 생성
  @Transactional
  public Conversation createGroup(String name, List<UUID> userIds) {
    Conversation conversation = Conversation.createGroup(name);
    conversationRepository.save(conversation);

    userIds.forEach(userId ->
        conversationParticipantRepository.save(
            ConversationParticipant.of(conversation, userId)));

    return conversation;
  }

  // 단건 조회
  public Conversation findById(UUID id) {
    return conversationRepository.findById(id)
        .orElseThrow(() -> new ConversationNotFoundException(id));
  }

  // 유저가 참여한 대화방 목록 조회
  public List<Conversation> findAllByUserId(UUID userId) {
    return conversationRepository.findAllByParticipantUserId(userId);
  }

  // 그룹 대화방 이름 수정
  @Transactional
  public Conversation updateName(UUID id, String name) {
    Conversation conversation = conversationRepository.findById(id)
        .orElseThrow(() -> new ConversationNotFoundException(id));

    conversation.updateName(name);
    return conversation;
  }
}
