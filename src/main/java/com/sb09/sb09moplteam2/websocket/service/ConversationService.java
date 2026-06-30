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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

  private final ConversationRepository conversationRepository;
  private final ConversationParticipantRepository conversationParticipantRepository;
  private final ConversationMapper conversationMapper;

  @Transactional
  public ConversationDto createDirect(UUID myUserId, UUID withUserId) {
    Conversation conversation = conversationParticipantRepository
        .findExistingDirectConversation(myUserId, withUserId)
        .orElseGet(() -> {
          Conversation newConversation = Conversation.createDirect();
          conversationRepository.save(newConversation);
          conversationParticipantRepository.save(
              ConversationParticipant.of(newConversation, myUserId));
          conversationParticipantRepository.save(
              ConversationParticipant.of(newConversation, withUserId));
          return newConversation;
        });

    return conversationMapper.toDto(conversation, myUserId);
  }

  public ConversationDto findById(UUID conversationId, UUID myUserId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    return conversationMapper.toDto(conversation, myUserId);
  }

  public ConversationDto findWithUser(UUID myUserId, UUID withUserId) {
    Conversation conversation = conversationParticipantRepository
        .findExistingDirectConversation(myUserId, withUserId)
        .orElseThrow(() -> new ConversationNotFoundException(null));
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
    // TODO: 커서 페이지네이션 쿼리 구현
    List<Conversation> conversations = conversationRepository
        .findAllByParticipantUserId(myUserId);

    List<UUID> conversationIds = conversations.stream()
        .map(Conversation::getId)
        .toList();

    // IN 쿼리로 참여자 한 번에 조회 (N+1 방지)
    Map<UUID, List<ConversationParticipant>> participantMap =
        conversationParticipantRepository.findByConversationIdIn(conversationIds)
            .stream()
            .collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));

    List<ConversationDto> data = conversations.stream()
        .map(c -> conversationMapper.toDto(
            c, myUserId, participantMap.getOrDefault(c.getId(), List.of())))
        .filter(dto -> keywordLike == null || keywordLike.isBlank()
            || (dto.with() != null && dto.with().name().contains(keywordLike)))
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
}
