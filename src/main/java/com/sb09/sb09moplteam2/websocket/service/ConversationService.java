package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.user.service.UserService;
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
  private final UserService userService;

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

    boolean hasKeyword = keywordLike != null && !keywordLike.isBlank();

    return hasKeyword
        ? findAllWithKeyword(myUserId, keywordLike, cursor, idAfter, limit, sortBy, sortDirection)
        : findAllWithoutKeyword(myUserId, cursor, idAfter, limit, sortBy, sortDirection);
  }

  // 기존 로직 (키워드 없을 때) - DB 커서 페이징 그대로 사용
  private CursorResponse<ConversationDto> findAllWithoutKeyword(
      UUID myUserId, String cursor, UUID idAfter, int limit,
      String sortBy, String sortDirection
  ) {
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

    return buildResponse(content, myUserId, hasNext, sortBy, sortDirection);
  }

  // 키워드 있을 때 - 전체 조회 후 애플리케이션 레벨 필터링 + 커서 페이징
  private CursorResponse<ConversationDto> findAllWithKeyword(
      UUID myUserId, String keywordLike, String cursor, UUID idAfter, int limit,
      String sortBy, String sortDirection
  ) {
    List<Conversation> all =
        conversationRepository.findAllByParticipantUserIdNoPaging(myUserId);

    // 참여자 배치 조회 (이름 필터링에 필요)
    List<UUID> allIds = all.stream().map(Conversation::getId).toList();
    Map<UUID, List<ConversationParticipant>> participantMap =
        conversationParticipantRepository.findByConversationIdIn(allIds)
            .stream()
            .collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));

    // 이름으로 필터링 (매퍼와 동일하게 상대방 UserSummary 조회)
    List<Conversation> filtered = all.stream()
        .filter(c -> matchesKeyword(c, myUserId, keywordLike, participantMap))
        .toList();

    // 커서 위치 찾기 (필터링된 리스트 기준)
    int startIndex = 0;
    if (cursor != null && idAfter != null) {
      Instant cursorLastMessageAt = Instant.parse(cursor);
      startIndex = indexAfterCursor(filtered, cursorLastMessageAt, idAfter);
    }

    int endIndex = Math.min(startIndex + limit, filtered.size());
    List<Conversation> content = filtered.subList(startIndex, endIndex);
    boolean hasNext = endIndex < filtered.size();

    return buildResponse(content, myUserId, participantMap, hasNext, sortBy, sortDirection);
  }

  private boolean matchesKeyword(Conversation c, UUID myUserId, String keywordLike,
      Map<UUID, List<ConversationParticipant>> participantMap) {
    List<ConversationParticipant> participants =
        participantMap.getOrDefault(c.getId(), List.of());
    return participants.stream()
        .map(ConversationParticipant::getUserId)
        .filter(id -> !id.equals(myUserId))
        .findFirst()
        .map(userService::getUserSummary)
        .map(summary -> summary.name().contains(keywordLike))
        .orElse(false);
  }

  private int indexAfterCursor(List<Conversation> filtered, Instant cursorLastMessageAt, UUID idAfter) {
    for (int i = 0; i < filtered.size(); i++) {
      Conversation c = filtered.get(i);
      boolean isBeforeCursor = c.getLastMessageAt().isBefore(cursorLastMessageAt)
          || (c.getLastMessageAt().equals(cursorLastMessageAt) && c.getId().compareTo(idAfter) < 0);
      if (isBeforeCursor) {
        return i;
      }
    }
    return filtered.size();
  }

  private CursorResponse<ConversationDto> buildResponse(
      List<Conversation> content, UUID myUserId, boolean hasNext,
      String sortBy, String sortDirection
  ) {
    List<UUID> conversationIds = content.stream().map(Conversation::getId).toList();
    Map<UUID, List<ConversationParticipant>> participantMap =
        conversationParticipantRepository.findByConversationIdIn(conversationIds)
            .stream()
            .collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));
    return buildResponse(content, myUserId, participantMap, hasNext, sortBy, sortDirection);
  }

  private CursorResponse<ConversationDto> buildResponse(
      List<Conversation> content, UUID myUserId,
      Map<UUID, List<ConversationParticipant>> participantMap,
      boolean hasNext, String sortBy, String sortDirection
  ) {
    List<ConversationDto> data = content.stream()
        .map(c -> conversationMapper.toDto(
            c, myUserId, participantMap.getOrDefault(c.getId(), List.of())))
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      Conversation last = content.get(content.size() - 1);
      nextCursor = last.getLastMessageAt().toString();
      nextIdAfter = last.getId();
    }

    return new CursorResponse<>(data, nextCursor, nextIdAfter, hasNext, data.size(), sortBy, sortDirection);
  }
}
