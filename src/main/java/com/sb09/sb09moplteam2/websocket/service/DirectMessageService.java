package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.event.message.MessageCreatedEvent;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.dto.response.DirectMessageResponse;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.mapper.DirectMessageMapper;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final DirectMessageMapper directMessageMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final UserRepository userRepository;

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
    log.debug("DM 목록 조회 요청: conversationId={}, myUserId={}, cursor={}, idAfter={}, limit={}",
        conversationId, myUserId, cursor, idAfter, limit);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("DM 목록 조회 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    // 참여자 확인 겸 배치 조회 (N+1 방지)
    List<ConversationParticipant> participants =
        conversationParticipantRepository.findByConversation(conversation);

    boolean isParticipant = participants.stream()
        .anyMatch(p -> p.getUserId().equals(myUserId));
    if (!isParticipant) {
      log.warn("DM 목록 조회 실패 - 참여자 아님: conversationId={}, myUserId={}",
          conversationId, myUserId);
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

    log.debug("DM 목록 조회 결과: conversationId={}, resultSize={}, hasNext={}",
        conversationId, data.size(), hasNext);

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

  // STOMP /app/dm/{conversationId}
  // DM 전송 → DB 저장 + conversation.lastMessageAt 갱신 → 브로드캐스트용 response 반환
  @Transactional
  public DirectMessageDto send(UUID conversationId, UUID senderId, String content) {
    log.debug("DM 전송 요청: conversationId={}, senderId={}", conversationId, senderId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("DM 전송 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    // 참여자 검증
    boolean isParticipant = conversationParticipantRepository
        .existsByConversationAndUserId(conversation, senderId);
    if (!isParticipant) {
      log.warn("DM 전송 실패 - 참여자 아님: conversationId={}, senderId={}",
          conversationId, senderId);
      throw new ConversationParticipantNotFoundException(conversationId, senderId);
    }

    // 메시지 저장
    DirectMessage dm = DirectMessage.of(conversation, senderId, content);
    directMessageRepository.save(dm);

    // conversation의 lastMessageAt 갱신 (커서 페이지네이션 정렬 기준)
    conversation.updateLastMessageAt(dm.getSentAt());

    log.info("DM 전송 완료: conversationId={}, messageId={}, senderId={}",
        conversationId, dm.getId(), senderId);

    // DM 수신 알림 이벤트
    User sender = userRepository.findById(senderId).orElseThrow(() -> UserNotFoundException.withId(senderId));
    UserSummary senderSummary = new UserSummary(senderId,sender.getName(),sender.getProfileImageUrl());

    ConversationParticipant receiverInfo = conversationParticipantRepository.findOtherParticipants(conversationId, senderId).orElseThrow(() -> new NoSuchElementException());
    User receiver = userRepository.findById(receiverInfo.getUserId()).orElseThrow(() -> UserNotFoundException.withId(receiverInfo.getUserId()));
    UserSummary receiverSummary = new UserSummary(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl());

    DirectMessageDto messageDto = new DirectMessageDto(dm.getId(),conversationId,dm.getSentAt(),senderSummary,receiverSummary,dm.getContent());
    eventPublisher.publishEvent(
        new MessageCreatedEvent(receiver.getId(),messageDto)
    );

    return messageDto;
  }

  // POST /api/conversations/{conversationId}/direct-messages/{directMessageId}/read
  // DM 읽음 처리 → lastReadAt 업데이트
  @Transactional
  public void read(UUID conversationId, UUID myUserId) {
    log.debug("DM 읽음 처리 요청: conversationId={}, myUserId={}", conversationId, myUserId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("DM 읽음 처리 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    conversationParticipantRepository
        .findByConversationAndUserId(conversation, myUserId)
        .orElseThrow(() -> {
          log.warn("DM 읽음 처리 실패 - 참여자 아님: conversationId={}, myUserId={}",
              conversationId, myUserId);
          return new ConversationParticipantNotFoundException(conversationId, myUserId);
        })
        .updateLastReadAt();

    log.info("DM 읽음 처리 완료: conversationId={}, myUserId={}", conversationId, myUserId);
  }
}