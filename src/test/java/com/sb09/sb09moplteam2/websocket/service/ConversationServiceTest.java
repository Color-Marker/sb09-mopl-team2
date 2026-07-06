package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
  private UUID withUserId;
  private UUID conversationId;
  private Conversation conversation;

  @BeforeEach
  void setUp() {
    myUserId = UUID.randomUUID();
    withUserId = UUID.randomUUID();
    conversationId = UUID.randomUUID();

    conversation = Conversation.createDirect();
    ReflectionTestUtils.setField(conversation, "id", conversationId);
  }

  // ───────────────────────────── createDirect ─────────────────────────────

  @Test
  void 기존_대화방이_없으면_신규_생성_후_반환한다() {
    ConversationDto expected = makeConversationDto();

    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, withUserId))
        .willReturn(Optional.empty());
    given(conversationRepository.save(any(Conversation.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(conversationMapper.toDto(any(Conversation.class), eq(myUserId)))
        .willReturn(expected);

    ConversationDto result = conversationService.createDirect(myUserId, withUserId);

    assertThat(result).isEqualTo(expected);
    verify(conversationRepository).save(any(Conversation.class));
    // 두 참여자 모두 저장되어야 함
    verify(conversationParticipantRepository, times(2)).save(any(ConversationParticipant.class));
  }

  @Test
  void 기존_대화방이_있으면_재사용_후_반환한다() {
    ConversationDto expected = makeConversationDto();

    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, withUserId))
        .willReturn(Optional.of(conversation));
    given(conversationMapper.toDto(eq(conversation), eq(myUserId)))
        .willReturn(expected);

    ConversationDto result = conversationService.createDirect(myUserId, withUserId);

    assertThat(result).isEqualTo(expected);
    // 신규 저장이 일어나지 않아야 함
    verify(conversationRepository, never()).save(any());
  }

  // ───────────────────────────── findById ─────────────────────────────

  @Test
  void findById_정상_조회한다() {
    ConversationDto expected = makeConversationDto();

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationMapper.toDto(conversation, myUserId)).willReturn(expected);

    ConversationDto result = conversationService.findById(conversationId, myUserId);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void findById_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationService.findById(conversationId, myUserId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  // ───────────────────────────── findWithUser ─────────────────────────────

  @Test
  void findWithUser_정상_조회한다() {
    ConversationDto expected = makeConversationDto();

    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, withUserId))
        .willReturn(Optional.of(conversation));
    given(conversationMapper.toDto(conversation, myUserId)).willReturn(expected);

    ConversationDto result = conversationService.findWithUser(myUserId, withUserId);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void findWithUser_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationParticipantRepository.findExistingDirectConversation(myUserId, withUserId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationService.findWithUser(myUserId, withUserId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  // ───────────────────────────── findAll ─────────────────────────────

  @Test
  void 첫_페이지_대화방_목록을_정상_조회한다() {
    ConversationParticipant participant = ConversationParticipant.of(conversation, withUserId);
    ConversationDto dto = makeConversationDto();

    given(conversationRepository.findAllByParticipantUserId(eq(myUserId), any(Pageable.class)))
        .willReturn(List.of(conversation));
    given(conversationParticipantRepository.findByConversationIdIn(List.of(conversationId)))
        .willReturn(List.of(participant));
    given(conversationMapper.toDto(eq(conversation), eq(myUserId), any()))
        .willReturn(dto);

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  void 커서가_있으면_커서_이후_대화방_목록을_조회한다() {
    Instant cursorLastMessageAt = Instant.now().minusSeconds(60);
    String cursor = cursorLastMessageAt.toString();
    UUID idAfter = UUID.randomUUID();
    ConversationParticipant participant = ConversationParticipant.of(conversation, withUserId);
    ConversationDto dto = makeConversationDto();

    given(conversationRepository.findAllByParticipantUserIdWithCursor(
        eq(myUserId), eq(cursorLastMessageAt), eq(idAfter), any(Pageable.class)))
        .willReturn(List.of(conversation));
    given(conversationParticipantRepository.findByConversationIdIn(List.of(conversationId)))
        .willReturn(List.of(participant));
    given(conversationMapper.toDto(eq(conversation), eq(myUserId), any()))
        .willReturn(dto);

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, cursor, idAfter, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void limit보다_결과가_많으면_hasNext가_true이고_nextCursor가_세팅된다() {
    int limit = 1;
    Conversation conversation2 = Conversation.createDirect();
    UUID conversationId2 = UUID.randomUUID();
    ReflectionTestUtils.setField(conversation2, "id", conversationId2);

    ConversationParticipant p1 = ConversationParticipant.of(conversation, withUserId);
    ConversationParticipant p2 = ConversationParticipant.of(conversation2, withUserId);
    ConversationDto dto1 = makeConversationDto();

    given(conversationRepository.findAllByParticipantUserId(eq(myUserId), any(Pageable.class)))
        .willReturn(List.of(conversation, conversation2)); // limit+1 개 반환
    given(conversationParticipantRepository.findByConversationIdIn(List.of(conversationId)))
        .willReturn(List.of(p1));
    given(conversationMapper.toDto(eq(conversation), eq(myUserId), any()))
        .willReturn(dto1);

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, null, null, null, limit, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isNotNull();
  }

  @Test
  void keywordLike가_있으면_상대방_이름으로_필터링된다() {
    UUID otherUserId1 = withUserId;
    UUID otherUserId2 = UUID.randomUUID();

    Conversation conversation2 = Conversation.createDirect();
    UUID conversationId2 = UUID.randomUUID();
    ReflectionTestUtils.setField(conversation2, "id", conversationId2);

    ConversationParticipant participant1 = ConversationParticipant.of(conversation, otherUserId1);
    ConversationParticipant participant2 = ConversationParticipant.of(conversation2, otherUserId2);

    UserSummary matchingSummary = new UserSummary(otherUserId1, "우디", null);
    UserSummary nonMatchingSummary = new UserSummary(otherUserId2, "버즈", null);

    ConversationDto matchingDto = new ConversationDto(conversationId, matchingSummary, null, false);

    // 전체 조회 (커서 없는 목록 - 키워드 검색 시 사용)
    given(conversationRepository.findAllByParticipantUserIdNoPaging(myUserId))
        .willReturn(List.of(conversation, conversation2));

    // findByConversationIdIn은 필터링/응답 빌드에 공용으로 딱 한 번 호출됨
    given(conversationParticipantRepository.findByConversationIdIn(List.of(conversationId, conversationId2)))
        .willReturn(List.of(participant1, participant2));

    // 이름 필터링에 쓰이는 userService 호출
    given(userService.getUserSummary(otherUserId1)).willReturn(matchingSummary);
    given(userService.getUserSummary(otherUserId2)).willReturn(nonMatchingSummary);

    given(conversationMapper.toDto(eq(conversation), eq(myUserId), any()))
        .willReturn(matchingDto);

    CursorResponse<ConversationDto> result = conversationService.findAll(
        myUserId, "우디", null, null, 10, "lastMessageAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).with().name()).isEqualTo("우디");
    assertThat(result.hasNext()).isFalse();

    // 한 번만 호출됐는지 확인 (효율성 검증 겸)
    verify(conversationParticipantRepository, times(1)).findByConversationIdIn(any());
  }

  // ───────────────────────────── 헬퍼 메서드 ─────────────────────────────

  private ConversationDto makeConversationDto() {
    return new ConversationDto(conversationId, null, null, false);
  }
}
