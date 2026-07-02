package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import com.sb09.sb09moplteam2.websocket.repository.ConversationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConversationParticipantServiceTest {

  @Mock
  private ConversationParticipantRepository conversationParticipantRepository;
  @Mock
  private ConversationRepository conversationRepository;

  @InjectMocks
  private ConversationParticipantService conversationParticipantService;

  private UUID conversationId;
  private UUID userId;
  private Conversation conversation;
  private ConversationParticipant participant;

  @BeforeEach
  void setUp() {
    conversationId = UUID.randomUUID();
    userId = UUID.randomUUID();

    conversation = Conversation.createDirect();
    ReflectionTestUtils.setField(conversation, "id", conversationId);

    participant = ConversationParticipant.of(conversation, userId);
  }

  // ───────────────────────────── join ─────────────────────────────

  @Test
  void join_정상적으로_참여하면_저장된_참여자를_반환한다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.existsByConversationAndUserId(conversation, userId))
        .willReturn(false);
    given(conversationParticipantRepository.save(any(ConversationParticipant.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    ConversationParticipant result = conversationParticipantService.join(conversationId, userId);

    assertThat(result.getUserId()).isEqualTo(userId);
    verify(conversationParticipantRepository).save(any(ConversationParticipant.class));
  }

  @Test
  void join_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationParticipantService.join(conversationId, userId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void join_이미_참여_중이면_IllegalStateException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.existsByConversationAndUserId(conversation, userId))
        .willReturn(true);

    assertThatThrownBy(() -> conversationParticipantService.join(conversationId, userId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(userId.toString());
  }

  // ───────────────────────────── findAllByConversationId ─────────────────────────────

  @Test
  void findAllByConversationId_정상_조회한다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversation(conversation))
        .willReturn(List.of(participant));

    List<ConversationParticipant> result =
        conversationParticipantService.findAllByConversationId(conversationId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUserId()).isEqualTo(userId);
  }

  @Test
  void findAllByConversationId_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(
        () -> conversationParticipantService.findAllByConversationId(conversationId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  // ───────────────────────────── updateLastReadAt ─────────────────────────────

  @Test
  void updateLastReadAt_정상적으로_lastReadAt이_갱신된다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversationAndUserId(conversation, userId))
        .willReturn(Optional.of(participant));

    ConversationParticipant result =
        conversationParticipantService.updateLastReadAt(conversationId, userId);

    assertThat(result.getLastReadAt()).isNotNull();
  }

  @Test
  void updateLastReadAt_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(
        () -> conversationParticipantService.updateLastReadAt(conversationId, userId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void updateLastReadAt_참여자가_아니면_ConversationParticipantNotFoundException을_던진다() {
    UUID strangerUserId = UUID.randomUUID();

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversationAndUserId(conversation, strangerUserId))
        .willReturn(Optional.empty());

    assertThatThrownBy(
        () -> conversationParticipantService.updateLastReadAt(conversationId, strangerUserId))
        .isInstanceOf(ConversationParticipantNotFoundException.class);
  }

  // ───────────────────────────── leave ─────────────────────────────

  @Test
  void leave_정상적으로_참여자가_삭제된다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversationAndUserId(conversation, userId))
        .willReturn(Optional.of(participant));

    conversationParticipantService.leave(conversationId, userId);

    verify(conversationParticipantRepository).delete(participant);
  }

  @Test
  void leave_대화방이_없으면_ConversationNotFoundException을_던진다() {
    given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationParticipantService.leave(conversationId, userId))
        .isInstanceOf(ConversationNotFoundException.class);
  }

  @Test
  void leave_참여자가_아니면_ConversationParticipantNotFoundException을_던진다() {
    UUID strangerUserId = UUID.randomUUID();

    given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));
    given(conversationParticipantRepository.findByConversationAndUserId(conversation, strangerUserId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> conversationParticipantService.leave(conversationId, strangerUserId))
        .isInstanceOf(ConversationParticipantNotFoundException.class);
  }
}
