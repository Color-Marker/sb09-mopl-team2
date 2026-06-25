package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import java.util.Map;
import java.util.stream.Collectors;
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
  private final DirectMessageRepository directMessageRepository;

  // POST /api/conversations
  // 1:1 대화 생성 (이미 있으면 기존 대화 반환)
  @Transactional
  public ConversationDto createDirect(UUID myUserId, UUID withUserId) {
    Conversation conversation = conversationParticipantRepository
        .findExistingDirectConversation(myUserId, withUserId)
        .orElseGet(() -> {
          Conversation newConversation = Conversation.createDirect();
          conversationRepository.save(newConversation);

          conversationParticipantRepository.save(
              com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant
                  .of(newConversation, myUserId));
          conversationParticipantRepository.save(
              com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant
                  .of(newConversation, withUserId));

          return newConversation;
        });

    return toDto(conversation, myUserId);
  }

  // GET /api/conversations/{conversationId}
  // 대화 단건 조회
  public ConversationDto findById(UUID conversationId, UUID myUserId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    return toDto(conversation, myUserId);
  }

  // GET /api/conversations/with?userId=
  // 특정 유저와의 대화 조회
  public ConversationDto findWithUser(UUID myUserId, UUID withUserId) {
    Conversation conversation = conversationParticipantRepository
        .findExistingDirectConversation(myUserId, withUserId)
        .orElseThrow(() -> new ConversationNotFoundException(null));

    return toDto(conversation, myUserId);
  }

  // GET /api/conversations
  // 내 대화 목록 조회 (커서 페이지네이션)
  public CursorResponse<ConversationDto> findAll(
      UUID myUserId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortBy,
      String sortDirection
  ) {
    // TODO: 커서 페이지네이션 쿼리 구현
    List<Conversation> conversations = conversationRepository
        .findAllByParticipantUserId(myUserId);

    // conversation ID 목록 추출
    List<UUID> conversationIds = conversations.stream()
        .map(Conversation::getId)
        .toList();

    // IN 쿼리로 참여자 한 번에 조회 (N+1 방지)
    List<ConversationParticipant> allParticipants = conversationParticipantRepository
        .findByConversationIdIn(conversationIds);

    // conversationId 기준으로 그룹핑
    Map<UUID, List<ConversationParticipant>> participantMap = allParticipants.stream()
        .collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));

    List<ConversationDto> data = conversations.stream()
        .map(c -> toDto(c, myUserId, participantMap.getOrDefault(c.getId(), List.of())))
        .toList();

    return new CursorResponse<>(
        data,
        null,
        null,
        false,
        data.size(),
        sortBy,
        sortDirection
    );
  }

  private ConversationDto toDto(Conversation conversation, UUID myUserId,
      List<ConversationParticipant> participants) {

    UUID withUserId = participants.stream()
        .map(ConversationParticipant::getUserId)
        .filter(id -> !id.equals(myUserId))
        .findFirst()
        .orElse(null);

    UserSummary with = new UserSummary(
        withUserId,
        null,   // TODO: userName
        null    // TODO: profileImageUrl
    );

    DirectMessageDto latestMessage = null; // TODO: 마지막 메시지 조회
    boolean hasUnread = false;             // TODO: lastReadAt 기준 계산

    return new ConversationDto(
        conversation.getId(),
        with,
        latestMessage,
        hasUnread
    );
  }
}
