package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.dto.response.DirectMessageResponse;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.websocket.mapper.DirectMessageMapper;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

  @Mock
  private DirectMessageRepository directMessageRepository;
  @Mock
  private ConversationRepository conversationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ConversationParticipantRepository conversationParticipantRepository;
  @Mock
  private DirectMessageMapper directMessageMapper;
  @Mock
  private org.springframework.context.ApplicationEventPublisher eventPublisher;
  @Mock
  private com.sb09.sb09moplteam2.user.repository.UserRepository userRepository;


  @InjectMocks
  private DirectMessageService directMessageService;

  private UUID conversationId;
  private UUID myUserId;
  private UUID otherUserId;
  private Conversation conversation;
  private ConversationParticipant myParticipant;
  private ConversationParticipant otherParticipant;
  private User sender;
  private User receiver;

  @BeforeEach
  void setUp() {
    conversationId = UUID.randomUUID();
    myUserId = UUID.randomUUID();
    otherUserId = UUID.randomUUID();

    conversation = Conversation.createDirect();
    ReflectionTestUtils.setField(conversation, "id", conversationId);

    myParticipant = ConversationParticipant.of(conversation, myUserId);
    otherParticipant = ConversationParticipant.of(conversation, otherUserId);

    sender = new User("보낸사람", "sender@test.com", "password");
    ReflectionTestUtils.setField(sender, "id", myUserId);

    receiver = new User("받는사람", "receiver@test.com", "password");
    ReflectionTestUtils.setField(receiver, "id", otherUserId);
  }

  // ───────────────────────────── findAll ─────────────────────────────

  @Test
  void 첫_페이지_DM_목록을_정상_조회한다() {
    int limit = 2;
    DirectMessage dm1 = makeDirectMessage(myUserId, "안녕");
    DirectMessage dm2 = makeDirectMessage(otherUserId, "반가워");
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversation(conversation))
        .willReturn(participants);
    given(directMessageRepository.findByConversationOrderBySentAtDesc(
        eq(conversation), any(Pageable.class)))
        .willReturn(List.of(dm1, dm2));
    given(directMessageMapper.toDto(eq(dm1), eq(participants))).willReturn(makeDmDto(dm1));
    given(directMessageMapper.toDto(eq(dm2), eq(participants))).willReturn(makeDmDto(dm2));

    CursorResponse<DirectMessageDto> result = directMessageService.findAll(
        conversationId, myUserId, null, null, limit, "sentAt", "DESCENDING");

    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  void 커서가_있으면_커서_이후_DM_목록을_조회한다() {
    int limit = 2;
    Instant cursorSentAt = Instant.now().minusSeconds(60);
    UUID idAfter = UUID.randomUUID();
    String cursor = cursorSentAt.toString();

    DirectMessage dm = makeDirectMessage(myUserId, "커서 이후 메시지");
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversation(conversation))
        .willReturn(participants);
    given(directMessageRepository.findByConversationWithCursor(
        eq(conversation), eq(cursorSentAt), eq(idAfter), any(Pageable.class)))
        .willReturn(List.of(dm));
    given(directMessageMapper.toDto(eq(dm), eq(participants))).willReturn(makeDmDto(dm));

    CursorResponse<DirectMessageDto> result = directMessageService.findAll(
        conversationId, myUserId, cursor, idAfter, limit, "sentAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void limit보다_결과가_많으면_hasNext가_true이고_nextCursor가_세팅된다() {
    int limit = 2;
    DirectMessage dm1 = makeDirectMessage(myUserId, "첫번째");
    DirectMessage dm2 = makeDirectMessage(myUserId, "두번째");
    DirectMessage dm3 = makeDirectMessage(myUserId, "세번째"); // limit+1 번째
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversation(conversation))
        .willReturn(participants);
    given(directMessageRepository.findByConversationOrderBySentAtDesc(
        eq(conversation), any(Pageable.class)))
        .willReturn(List.of(dm1, dm2, dm3));
    given(directMessageMapper.toDto(eq(dm1), eq(participants))).willReturn(makeDmDto(dm1));
    given(directMessageMapper.toDto(eq(dm2), eq(participants))).willReturn(makeDmDto(dm2));

    CursorResponse<DirectMessageDto> result = directMessageService.findAll(
        conversationId, myUserId, null, null, limit, "sentAt", "DESCENDING");

    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isNotNull();
  }

  @Test
  void findAll_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> directMessageService.findAll(
        conversationId, myUserId, null, null, 10, "sentAt", "DESCENDING"))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void findAll_참여자가_아니면_ConversationParticipantNotFoundException을_던진다() {
    UUID strangerUserId = UUID.randomUUID();

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversation(conversation))
        .willReturn(List.of(myParticipant, otherParticipant));

    assertThatThrownBy(() -> directMessageService.findAll(
        conversationId, strangerUserId, null, null, 10, "sentAt", "DESCENDING"))
        .isInstanceOf(ConversationParticipantNotFoundException.class);
  }

  // ───────────────────────────── send ─────────────────────────────

  @Test
  void DM을_전송하면_저장되고_response를_반환한다() {
    String content = "안녕하세요";
    User sender = mock(User.class);
    User receiver = mock(User.class);
    given(sender.getName()).willReturn("sender");
    given(receiver.getName()).willReturn("receiver");
    given(receiver.getId()).willReturn(otherUserId);

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.existsByConversationAndUserId(conversation, myUserId))
        .willReturn(true);
    given(directMessageRepository.save(any(DirectMessage.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(userRepository.findById(myUserId)).willReturn(Optional.of(sender));
    given(conversationParticipantRepository.findOtherParticipants(conversationId, myUserId))
        .willReturn(Optional.of(otherParticipant));
    given(userRepository.findById(otherUserId)).willReturn(Optional.of(receiver));

    DirectMessageResponse response = directMessageService.send(conversationId, myUserId, content);

    assertThat(response.conversationId()).isEqualTo(conversationId);
    assertThat(response.senderId()).isEqualTo(myUserId);
    assertThat(response.content()).isEqualTo(content);
    assertThat(response.sentAt()).isNotNull();
    verify(directMessageRepository).save(any(DirectMessage.class));
  }

  @Test
  void DM_전송_시_conversation의_lastMessageAt이_갱신된다() {
    String content = "갱신 테스트";
    Instant before = conversation.getLastMessageAt();
    User sender = mock(User.class);
    User receiver = mock(User.class);
    given(sender.getName()).willReturn("sender");
    given(receiver.getName()).willReturn("receiver");
    given(receiver.getId()).willReturn(otherUserId);

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.existsByConversationAndUserId(conversation, myUserId))
        .willReturn(true);
    given(directMessageRepository.save(any(DirectMessage.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(userRepository.findById(myUserId)).willReturn(Optional.of(sender));
    given(conversationParticipantRepository.findOtherParticipants(conversationId, myUserId))
        .willReturn(Optional.of(otherParticipant));
    given(userRepository.findById(otherUserId)).willReturn(Optional.of(receiver));

    directMessageService.send(conversationId, myUserId, content);

    assertThat(conversation.getLastMessageAt()).isAfterOrEqualTo(before);
  }

  @Test
  void send_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> directMessageService.send(conversationId, myUserId, "내용"))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void send_참여자가_아니면_ConversationParticipantNotFoundException을_던진다() {
    UUID strangerUserId = UUID.randomUUID();

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.existsByConversationAndUserId(conversation, strangerUserId))
        .willReturn(false);

    assertThatThrownBy(() -> directMessageService.send(conversationId, strangerUserId, "내용"))
        .isInstanceOf(ConversationParticipantNotFoundException.class);
  }

  // ───────────────────────────── read ─────────────────────────────

  @Test
  void read_정상적으로_읽음_처리된다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversationAndUserId(conversation, myUserId))
        .willReturn(Optional.of(myParticipant));

    directMessageService.read(conversationId, myUserId);

    assertThat(myParticipant.getLastReadAt()).isNotNull();
  }

  @Test
  void read_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> directMessageService.read(conversationId, myUserId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void read_참여자가_아니면_ConversationParticipantNotFoundException을_던진다() {
    UUID strangerUserId = UUID.randomUUID();

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversationAndUserId(conversation, strangerUserId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> directMessageService.read(conversationId, strangerUserId))
        .isInstanceOf(ConversationParticipantNotFoundException.class);
  }

  // ───────────────────────────── 헬퍼 메서드 ─────────────────────────────

  private DirectMessage makeDirectMessage(UUID senderId, String content) {
    DirectMessage dm = DirectMessage.of(conversation, senderId, content);
    ReflectionTestUtils.setField(dm, "id", UUID.randomUUID());
    return dm;
  }

  private DirectMessageDto makeDmDto(DirectMessage dm) {
    return new DirectMessageDto(
        dm.getId(),
        conversationId,
        dm.getSentAt(),
        null,
        null,
        dm.getContent()
    );
  }
}
