package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.mapper.DirectMessageMapper;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final DirectMessageMapper directMessageMapper;

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

    // 참여자 확인 겸 배치 조회 (N+1 방지)
    List<ConversationParticipant> participants =
        conversationParticipantRepository.findByConversation(conversation);

    boolean isParticipant = participants.stream()
        .anyMatch(p -> p.getUserId().equals(myUserId));
    if (!isParticipant) {
      throw new ConversationParticipantNotFoundException(conversationId, myUserId);
    }

    Pageable pageable = PageRequest.of(0, limit + 1);
    List<DirectMessage> messages;

    if (cursor != null && idAfter != null) {
      Instant cursorSentAt = Instant.parse(cursor);
      messages = directMessageRepository.findByConversationWithCursor(
          conversation, cursorSentAt, idAfter, pageable);
    } else {
      messages = directMessageRepository.findByConversationOrderBySentAtDesc(
          conversation, pageable);
    }

    boolean hasNext = messages.size() > limit;
    List<DirectMessage> content = hasNext ? messages.subList(0, limit) : messages;

    List<DirectMessageDto> data = content.stream()
        .map(dm -> directMessageMapper.toDto(dm, participants))
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      DirectMessage last = content.get(content.size() - 1);
      nextCursor = last.getSentAt().toString();
      nextIdAfter = last.getId();
    }

    return new CursorResponse<>(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
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
}
