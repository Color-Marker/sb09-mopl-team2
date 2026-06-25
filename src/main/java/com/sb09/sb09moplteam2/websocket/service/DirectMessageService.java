package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;

  // GET /api/conversations/{conversationId}/direct-messages
  // DM 목록 조회 (커서 페이지네이션) - 참여자만 조회 가능
  public CursorResponse<DirectMessageDto> findAll(
      UUID conversationId,
      UUID myUserId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortBy,
      String sortDirection
  ) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    // 참여자가 아니면 조회 불가
    if (!conversationParticipantRepository.existsByConversationAndUserId(conversation, myUserId)) {
      throw new ConversationParticipantNotFoundException(conversationId, myUserId);
    }

    // TODO: 커서 페이지네이션 쿼리 구현
    List<DirectMessage> messages = directMessageRepository
        .findByConversationOrderBySentAtDesc(conversation, org.springframework.data.domain.PageRequest.of(0, limit))
        .getContent();

    List<DirectMessageDto> data = messages.stream()
        .map(dm -> toDto(dm, conversation))
        .toList();

    return new CursorResponse<>(
        data,
        null,       // TODO: nextCursor 계산
        null,       // TODO: nextIdAfter 계산
        false,      // TODO: hasNext 계산
        data.size(),
        sortBy,
        sortDirection
    );
  }

  // POST /api/conversations/{conversationId}/direct-messages/{directMessageId}/read
  // DM 읽음 처리 → lastReadAt 업데이트
  @Transactional
  public void read(UUID conversationId, UUID myUserId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    conversationParticipantRepository
        .findByConversationAndUserId(conversation, myUserId)
        .orElseThrow(() -> new ConversationParticipantNotFoundException(conversationId, myUserId))
        .updateLastReadAt();
  }

  private DirectMessageDto toDto(DirectMessage dm, Conversation conversation) {
    // TODO: 팀원 User 도메인 연동 후 실제 UserSummary로 교체
    UserSummary sender = new UserSummary(
        dm.getSenderId(),
        null,   // TODO: senderName
        null    // TODO: senderProfileImageUrl
    );

    // 수신자 = 참여자 중 발신자가 아닌 사람
    UUID receiverId = conversationParticipantRepository
        .findByConversation(conversation)
        .stream()
        .map(cp -> cp.getUserId())
        .filter(id -> !id.equals(dm.getSenderId()))
        .findFirst()
        .orElse(null);

    UserSummary receiver = new UserSummary(
        receiverId,
        null,   // TODO: receiverName
        null    // TODO: receiverProfileImageUrl
    );

    return new DirectMessageDto(
        dm.getId(),
        conversation.getId(),
        dm.getSentAt(),
        sender,
        receiver,
        dm.getContent()
    );
  }
}
