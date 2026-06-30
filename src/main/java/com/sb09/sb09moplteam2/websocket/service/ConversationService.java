package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.websocket.mapper.ConversationMapper;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final ConversationMapper conversationMapper;

  @Transactional
  public ConversationDto createDirect(UUID myUserId, UUID withUserId) {
    log.debug("DM 대화방 생성 요청: myUserId={}, withUserId={}", myUserId, withUserId);

    Conversation conversation = conversationParticipantRepository
        .findExistingDirectConversation(myUserId, withUserId)
        .orElseGet(() -> {
          Conversation newConversation = Conversation.createDirect();
          conversationRepository.save(newConversation);
          conversationParticipantRepository.save(
              ConversationParticipant.of(newConversation, myUserId));
          conversationParticipantRepository.save(
              ConversationParticipant.of(newConversation, withUserId));
          log.info("DM 대화방 신규 생성 완료: conversationId={}, myUserId={}, withUserId={}",
              newConversation.getId(), myUserId, withUserId);
          return newConversation;
        });

    return conversationMapper.toDto(conversation, myUserId);
  }

  public ConversationDto findById(UUID conversationId, UUID myUserId) {
    log.debug("대화방 단건 조회: conversationId={}, myUserId={}", conversationId, myUserId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> {
          log.warn("대화방 조회 실패 - 존재하지 않음: conversationId={}", conversationId);
          return new ConversationNotFoundException(conversationId);
        });
    return conversationMapper.toDto(conversation, myUserId);
  }

  public ConversationDto findWithUser(UUID myUserId, UUID withUserId) {
    log.debug("상대방 기준 DM 대화방 조회: myUserId={}, withUserId={}", myUserId, withUserId);

    Conversation conversation = conversationParticipantRepository
        .findExistingDirectConversation(myUserId, withUserId)
        .orElseThrow(() -> {
          log.warn("DM 대화방 조회 실패 - 존재하지 않음: myUserId={}, withUserId={}",
              myUserId, withUserId);
          return new ConversationNotFoundException(null);
        });
    return conversationMapper.toDto(conversation, myUserId);
  }

  public CursorResponse<ConversationDto> findAll(
      UUID myUserId,
      String keywordLike,
      String cursor,
      UUID idAfter,
      int limit,
      String sortBy,
      String sortDirection
  ) {
    log.debug("대화방 목록 조회: myUserId={}, keywordLike={}, cursor={}, idAfter={}, limit={}",
        myUserId, keywordLike, cursor, idAfter, limit);

    Pageable pageable = PageRequest.of(0, limit + 1);
    List<Conversation> conversations;

    if (cursor != null && idAfter != null) {
      Instant cursorLastMessageAt = Instant.parse(cursor);
      conversations = conversationRepository.findAllByParticipantUserIdWithCursor(
          myUserId, cursorLastMessageAt, idAfter, pageable);
    } else {
      conversations = conversationRepository.findAllByParticipantUserId(myUserId, pageable);
    }

    boolean hasNext = conversations.size() > limit;
    List<Conversation> content = hasNext ? conversations.subList(0, limit) : conversations;

    List<UUID> conversationIds = content.stream()
        .map(Conversation::getId)
        .toList();

    // IN 쿼리로 참여자 한 번에 조회 (N+1 방지)
    Map<UUID, List<ConversationParticipant>> participantMap =
        conversationParticipantRepository.findByConversationIdIn(conversationIds)
            .stream()
            .collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));

    List<ConversationDto> data = content.stream()
        .map(c -> conversationMapper.toDto(
            c, myUserId, participantMap.getOrDefault(c.getId(), List.of())))
        .filter(dto -> keywordLike == null || keywordLike.isBlank()
            || (dto.with() != null && dto.with().name().contains(keywordLike)))
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      Conversation last = content.get(content.size() - 1);
      nextCursor = last.getLastMessageAt().toString();
      nextIdAfter = last.getId();
    }

    log.debug("대화방 목록 조회 결과: myUserId={}, resultSize={}, hasNext={}",
        myUserId, data.size(), hasNext);

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
}
