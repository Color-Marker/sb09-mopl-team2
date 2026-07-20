package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.mapper.ConversationMapper;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

  @Mock
  private ConversationRepository conversationRepository;
  @Mock
  private ConversationParticipantRepository conversationParticipantRepository;
  @Mock
  private ConversationMapper conversationMapper;
  @Mock
  private UserService userService;

  @InjectMocks
  private ConversationService conversationService;

  private UUID myUserId;
  private UUID otherUserId;

  @BeforeEach
  void setUp() {
    myUserId = UUID.randomUUID();
    otherUserId = UUID.randomUUID();
  }

  private Conversation makeConversation(Instant lastMessageAt) {
    Conversation conversation = Conversation.createDirect();
    ReflectionTestUtils.setField(conversation, "id", UUID.randomUUID());
    conversation.updateLastMessageAt(lastMessageAt);
    return conversation;
  }

  // ───────────────────────────── createDirect ─────────────────────────────

  @Test
  void createDirect_기존_대화방이_있으면_그대로_반환한다() {
    Conversation existing = makeConversation(Instant.now());
    ConversationDto dto = new ConversationDto(existing.getId(), null, null, false);

    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, otherUserId))
        .willReturn(Optional.of(existing));
    given(conversationMapper.toDto(existing, myUserId)).willReturn(dto);

    ConversationDto result = conversationService.createDirect(myUserId, otherUserId);

    assertThat(result).isEqualTo(dto);
    verify(conversationRepository, never()).save(any());
    verify(conversationParticipantRepository, never()).save(any());
  }

  @Test
  void createDirect_기존_대화방이_없으면_신규_생성한다() {
    ConversationDto dto = new ConversationDto(UUID.randomUUID(), null, null, false);

    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, otherUserId))
        .willReturn(Optional.empty());
    given(conversationMapper.toDto(any(Conversation.class), eq(myUserId))).willReturn(dto);

    ConversationDto result = conversationService.createDirect(myUserId, otherUserId);

    assertThat(result).isEqualTo(dto);
    verify(conversationRepository).save(any(Conversation.class));
    verify(conversationParticipantRepository, org.mockito.Mockito.times(2)).save(any(ConversationParticipant.class));
  }

  // ───────────────────────────── findById ─────────────────────────────

  @Test
  void findById_존재하면_dto를_반환한다() {
    Conversation conversation = makeConversation(Instant.now());
    ConversationDto dto = new ConversationDto(conversation.getId(), null, null, false);

    given(conversationRepository.findById(conversation.getId())).willReturn(Optional.of(conversation));
    given(conversationMapper.toDto(conversation, myUserId)).willReturn(dto);

    ConversationDto result = conversationService.findById(conversation.getId(), myUserId);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void findById_존재하지_않으면_예외를_던진다() {
    UUID conversationId = UUID.randomUUID();
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationService.findById(conversationId, myUserId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  // ───────────────────────────── findWithUser ─────────────────────────────

  @Test
  void findWithUser_존재하면_dto를_반환한다() {
    Conversation conversation = makeConversation(Instant.now());
    ConversationDto dto = new ConversationDto(conversation.getId(), null, null, false);

    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, otherUserId))
        .willReturn(Optional.of(conversation));
    given(conversationMapper.toDto(conversation, myUserId)).willReturn(dto);

    ConversationDto result = conversationService.findWithUser(myUserId, otherUserId);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void findWithUser_존재하지_않으면_예외를_던진다() {
    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, otherUserId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationService.findWithUser(myUserId, otherUserId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  // ───────────────────────────── findAll (키워드 분기) ─────────────────────────────

  @Test
  void findAll_키워드가_없으면_findAllWithoutKeyword_경로를_탄다() {
    given(conversationRepository.findAllByParticipantUserId(eq(myUserId), any(Pageable.class)))
        .willReturn(List.of());
    given(conversationParticipantRepository.findByConversationIdIn(List.of()))
        .willReturn(List.of());

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).isEmpty();
    verify(conversationRepository).findAllByParticipantUserId(eq(myUserId), any(Pageable.class));
  }

  @Test
  void findAll_키워드가_공백이면_findAllWithoutKeyword_경로를_탄다() {
    given(conversationRepository.findAllByParticipantUserId(eq(myUserId), any(Pageable.class)))
        .willReturn(List.of());
    given(conversationParticipantRepository.findByConversationIdIn(List.of()))
        .willReturn(List.of());

    conversationService.findAll(myUserId, "  ", null, null, 10, "lastMessageAt", "DESCENDING");

    verify(conversationRepository).findAllByParticipantUserId(eq(myUserId), any(Pageable.class));
  }

  @Test
  void findAll_키워드가_있으면_findAllWithKeyword_경로를_탄다() {
    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of());

    conversationService.findAll(myUserId, "검색어", null, null, 10, "lastMessageAt", "DESCENDING");

    verify(conversationRepository).findAllByParticipantUserIdNoPaging(myUserId);
    verify(conversationRepository, never()).findAllByParticipantUserId(any(), any());
  }

  // ───────────────────────────── findAllWithoutKeyword (커서/hasNext 분기) ─────────────────────────────

  @Test
  void 커서없이_조회하면_findAllByParticipantUserId를_사용한다() {
    Conversation c1 = makeConversation(Instant.now());
    given(conversationRepository.findAllByParticipantUserId(eq(myUserId), any(Pageable.class)))
        .willReturn(List.of(c1));
    given(conversationParticipantRepository.findByConversationIdIn(any()))
        .willReturn(List.of());
    given(conversationMapper.toDto(eq(c1), eq(myUserId), any()))
        .willReturn(new ConversationDto(c1.getId(), null, null, false));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  void 커서가_있으면_findAllByParticipantUserIdWithCursor를_사용한다() {
    Instant cursorTime = Instant.now().minusSeconds(60);
    UUID idAfter = UUID.randomUUID();
    Conversation c1 = makeConversation(Instant.now());

    given(conversationRepository.findAllByParticipantUserIdWithCursor(
        eq(myUserId), eq(cursorTime), eq(idAfter), any(Pageable.class)))
        .willReturn(List.of(c1));
    given(conversationParticipantRepository.findByConversationIdIn(any()))
        .willReturn(List.of());
    given(conversationMapper.toDto(eq(c1), eq(myUserId), any()))
        .willReturn(new ConversationDto(c1.getId(), null, null, false));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, cursorTime.toString(), idAfter, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
  }

  @Test
  void limit보다_결과가_많으면_hasNext가_true이고_nextCursor가_세팅된다() {
    Conversation c1 = makeConversation(Instant.now().minusSeconds(10));
    Conversation c2 = makeConversation(Instant.now());
    int limit = 1;

    given(conversationRepository.findAllByParticipantUserId(eq(myUserId), any(Pageable.class)))
        .willReturn(List.of(c1, c2));
    given(conversationParticipantRepository.findByConversationIdIn(any()))
        .willReturn(List.of());
    given(conversationMapper.toDto(eq(c1), eq(myUserId), any()))
        .willReturn(new ConversationDto(c1.getId(), null, null, false));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, null, null, limit, "lastMessageAt", "DESCENDING");

    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isEqualTo(c1.getId());
  }

  // ───────────────────────────── findAllWithKeyword (matchesKeyword 분기) ─────────────────────────────

  @Test
  void 키워드가_상대방_이름에_포함되면_필터링에_통과한다() {
    Conversation c1 = makeConversation(Instant.now());
    ConversationParticipant myP = ConversationParticipant.of(c1, myUserId);
    ConversationParticipant otherP = ConversationParticipant.of(c1, otherUserId);

    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of(c1));
    given(conversationParticipantRepository.findByConversationIdIn(List.of(c1.getId())))
        .willReturn(List.of(myP, otherP));
    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "홍길동", null));
    given(conversationMapper.toDto(eq(c1), eq(myUserId), any()))
        .willReturn(new ConversationDto(c1.getId(), null, null, false));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, "길동", null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
  }

  @Test
  void 키워드가_상대방_이름에_없으면_필터링에서_제외된다() {
    Conversation c1 = makeConversation(Instant.now());
    ConversationParticipant myP = ConversationParticipant.of(c1, myUserId);
    ConversationParticipant otherP = ConversationParticipant.of(c1, otherUserId);

    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of(c1));
    given(conversationParticipantRepository.findByConversationIdIn(List.of(c1.getId())))
        .willReturn(List.of(myP, otherP));
    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "홍길동", null));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, "존재안함", null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).isEmpty();
  }

  @Test
  void 상대방_참여자가_없으면_필터링에서_제외된다() {
    Conversation c1 = makeConversation(Instant.now());
    ConversationParticipant myP = ConversationParticipant.of(c1, myUserId);

    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of(c1));
    given(conversationParticipantRepository.findByConversationIdIn(List.of(c1.getId())))
        .willReturn(List.of(myP));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, "아무거나", null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).isEmpty();
  }

  // ───────────────────────────── findAllWithKeyword (커서/hasNext 분기) ─────────────────────────────

  @Test
  void 키워드_검색에_커서가_있으면_커서_이후_항목만_반환한다() {
    Instant t1 = Instant.now().minusSeconds(120);
    Instant t2 = Instant.now().minusSeconds(60);
    Conversation older = makeConversation(t1);
    Conversation newer = makeConversation(t2);
    ConversationParticipant myP1 = ConversationParticipant.of(older, myUserId);
    ConversationParticipant otherP1 = ConversationParticipant.of(older, otherUserId);
    ConversationParticipant myP2 = ConversationParticipant.of(newer, myUserId);
    ConversationParticipant otherP2 = ConversationParticipant.of(newer, otherUserId);

    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of(newer, older));
    given(conversationParticipantRepository.findByConversationIdIn(any()))
        .willReturn(List.of(myP1, otherP1, myP2, otherP2));
    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "검색대상", null));
    given(conversationMapper.toDto(eq(older), eq(myUserId), any()))
        .willReturn(new ConversationDto(older.getId(), null, null, false));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, "검색", t2.toString(), newer.getId(), 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
  }

  @Test
  void 키워드_검색_결과가_limit보다_많으면_hasNext가_true다() {
    Conversation c1 = makeConversation(Instant.now().minusSeconds(10));
    Conversation c2 = makeConversation(Instant.now());
    ConversationParticipant myP1 = ConversationParticipant.of(c1, myUserId);
    ConversationParticipant otherP1 = ConversationParticipant.of(c1, otherUserId);
    ConversationParticipant myP2 = ConversationParticipant.of(c2, myUserId);
    ConversationParticipant otherP2 = ConversationParticipant.of(c2, otherUserId);

    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of(c2, c1));
    given(conversationParticipantRepository.findByConversationIdIn(any()))
        .willReturn(List.of(myP1, otherP1, myP2, otherP2));
    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "검색어유저", null));
    given(conversationMapper.toDto(any(Conversation.class), eq(myUserId), any()))
        .willReturn(new ConversationDto(UUID.randomUUID(), null, null, false));

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, "검색", null, null, 1, "lastMessageAt", "DESCENDING");

    assertThat(result.hasNext()).isTrue();
  }
}
