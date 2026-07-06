package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantAlreadyExistsException;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationParticipantService {

  private final ConversationParticipantRepository conversationParticipantRepository;
  private final ConversationRepository conversationRepository;

  // 참여자 추가
  @Transactional
  public ConversationParticipant join(UUID conversationId, UUID userId) {
    log.debug("참여자 추가 요청: conversationId={}, userId={}", conversationId, userId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("참여자 추가 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    // 이미 참여 중이면 중복 추가 방지
    if (conversationParticipantRepository.existsByConversationAndUserId(conversation, userId)) {
      log.warn("참여자 추가 실패 - 이미 참여 중: conversationId={}, userId={}",
          conversationId, userId);
      throw new ConversationParticipantAlreadyExistsException(conversationId, userId);
    }

    ConversationParticipant participant = ConversationParticipant.of(conversation, userId);
    ConversationParticipant saved = conversationParticipantRepository.save(participant);

    log.info("참여자 추가 완료: conversationId={}, userId={}", conversationId, userId);
    return saved;
  }

  // 대화방 참여자 목록 조회
  public List<ConversationParticipant> findAllByConversationId(UUID conversationId) {
    log.debug("참여자 목록 조회: conversationId={}", conversationId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("참여자 목록 조회 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    List<ConversationParticipant> participants =
        conversationParticipantRepository.findByConversation(conversation);

    log.debug("참여자 목록 조회 결과: conversationId={}, count={}",
        conversationId, participants.size());

    return participants;
  }

  // 마지막 읽은 시각 업데이트 (메시지 읽음 처리)
  @Transactional
  public ConversationParticipant updateLastReadAt(UUID conversationId, UUID userId) {
    log.debug("읽음 처리 요청: conversationId={}, userId={}", conversationId, userId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("읽음 처리 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    ConversationParticipant participant = conversationParticipantRepository
        .findByConversationAndUserId(conversation, userId)
        .orElseThrow(() -> {
          log.warn("읽음 처리 실패 - 참여자 아님: conversationId={}, userId={}",
              conversationId, userId);
          return new ConversationParticipantNotFoundException(conversationId, userId);
        });

    participant.updateLastReadAt();
    log.info("읽음 처리 완료: conversationId={}, userId={}", conversationId, userId);

    return participant; // dirty checking으로 자동 반영
  }

  // 참여자 나가기
  @Transactional
  public void leave(UUID conversationId, UUID userId) {
    log.debug("참여자 나가기 요청: conversationId={}, userId={}", conversationId, userId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("참여자 나가기 실패 - 대화방 없음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });

    ConversationParticipant participant = conversationParticipantRepository
        .findByConversationAndUserId(conversation, userId)
        .orElseThrow(() -> {
          log.warn("참여자 나가기 실패 - 참여자 아님: conversationId={}, userId={}",
              conversationId, userId);
          return new ConversationParticipantNotFoundException(conversationId, userId);
        });

    conversationParticipantRepository.delete(participant);
    log.info("참여자 나가기 완료: conversationId={}, userId={}", conversationId, userId);
  }
}
