package com.sb09.sb09moplteam2.websocket.mapper;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConversationMapper {

  private final UserService userService;
  private final ConversationParticipantRepository participantRepository;
  private final DirectMessageRepository directMessageRepository;
  private final DirectMessageMapper directMessageMapper;

  // 단건 조회용 (매퍼가 직접 participants 조회)
  public ConversationDto toDto(Conversation conversation, UUID currentUserId) {
    List<ConversationParticipant> participants =
        participantRepository.findByConversation(conversation);
    return toDto(conversation, currentUserId, participants);
  }

  // 목록 조회용 (서비스에서 배치 조회한 participants 전달 → N+1 방지)
  public ConversationDto toDto(Conversation conversation, UUID currentUserId,
      List<ConversationParticipant> participants) {

    UserSummary with = participants.stream()
        .map(ConversationParticipant::getUserId)
        .filter(id -> !id.equals(currentUserId))
        .findFirst()
        .map(userService::getUserSummary)
        .orElse(null);

    DirectMessageDto latestMessage = directMessageRepository
        .findTopByConversationIdOrderBySentAtDesc(conversation.getId())
        .map(directMessageMapper::toDto)
        .orElse(null);

    boolean hasUnread = calculateHasUnread(conversation.getId(), currentUserId, participants);

    return new ConversationDto(
        conversation.getId(),
        with,
        latestMessage,
        hasUnread
    );
  }

  private boolean calculateHasUnread(UUID conversationId, UUID currentUserId,
      List<ConversationParticipant> participants) {

    return participants.stream()
        .filter(p -> p.getUserId().equals(currentUserId))
        .findFirst()
        .map(me -> {
          Instant lastReadAt = me.getLastReadAt();
          if (lastReadAt == null) {
            return directMessageRepository.existsByConversationId(conversationId);
          }
          return directMessageRepository
              .existsByConversationIdAndSenderIdNotAndSentAtAfter(
                  conversationId, currentUserId, lastReadAt
              );
        })
        .orElse(false);
  }
}
