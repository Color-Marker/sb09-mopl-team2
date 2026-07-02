package com.sb09.sb09moplteam2.websocket.mapper;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

  private final UserService userService;
  private final ConversationParticipantRepository participantRepository;

  // 단건용 (매퍼가 직접 participants 조회)
  public DirectMessageDto toDto(DirectMessage message) {
    List<ConversationParticipant> participants =
        participantRepository.findByConversation(message.getConversation());
    return toDto(message, participants);
  }

  // 목록 조회용 (서비스에서 배치 조회한 participants 전달 → N+1 방지)
  public DirectMessageDto toDto(DirectMessage message, List<ConversationParticipant> participants) {
    UserSummary sender = userService.getUserSummary(message.getSenderId());

    UserSummary receiver = participants.stream()
        .map(ConversationParticipant::getUserId)
        .filter(id -> !id.equals(message.getSenderId()))
        .findFirst()
        .map(userService::getUserSummary)
        .orElse(null);

    return new DirectMessageDto(
        message.getId(),
        message.getConversation().getId(),
        message.getSentAt(),
        sender,
        receiver,
        message.getContent()
    );
  }
}
