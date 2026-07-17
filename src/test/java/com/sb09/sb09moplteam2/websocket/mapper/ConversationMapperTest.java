package com.sb09.sb09moplteam2.websocket.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConversationMapperTest {

  @Mock
  private UserService userService;
  @Mock
  private ConversationParticipantRepository participantRepository;
  @Mock
  private DirectMessageRepository directMessageRepository;
  @Mock
  private DirectMessageMapper directMessageMapper;

  @InjectMocks
  private ConversationMapper conversationMapper;

  private UUID conversationId;
  private UUID myUserId;
  private UUID otherUserId;
  private Conversation conversation;
  private ConversationParticipant myParticipant;
  private ConversationParticipant otherParticipant;

  @BeforeEach
  void setUp() {
    conversationId = UUID.randomUUID();
    myUserId = UUID.randomUUID();
    otherUserId = UUID.randomUUID();

    conversation = Conversation.createDirect();
    ReflectionTestUtils.setField(conversation, "id", conversationId);

    myParticipant = ConversationParticipant.of(conversation, myUserId);
    otherParticipant = ConversationParticipant.of(conversation, otherUserId);
  }

  @Test
  void 단건_조회용_toDto는_참여자를_직접_조회해서_위임한다() {
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);
    UserSummary otherSummary = new UserSummary(otherUserId, "상대방", null);

    given(participantRepository.findByConversation(conversation)).willReturn(participants);
    given(userService.getUserSummary(otherUserId)).willReturn(otherSummary);
    given(directMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId))
        .willReturn(Optional.empty());

    ConversationDto result = conversationMapper.toDto(conversation, myUserId);

    assertThat(result.id()).isEqualTo(conversationId);
    assertThat(result.with()).isEqualTo(otherSummary);
    verify(participantRepository).findByConversation(conversation);
  }

  @Test
  void 상대방_정보와_최신_메시지를_포함한_ConversationDto를_생성한다() {
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);
    UserSummary otherSummary = new UserSummary(otherUserId, "상대방", "profile.jpg");
    DirectMessage lastMessage = DirectMessage.of(conversation, otherUserId, "마지막 메시지");
    DirectMessageDto lastMessageDto = new DirectMessageDto(
        UUID.randomUUID(), conversationId, Instant.now(), null, null, "마지막 메시지");

    given(userService.getUserSummary(otherUserId)).willReturn(otherSummary);
    given(directMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId))
        .willReturn(Optional.of(lastMessage));
    given(directMessageMapper.toDto(lastMessage)).willReturn(lastMessageDto);

    ConversationDto result = conversationMapper.toDto(conversation, myUserId, participants);

    assertThat(result.with()).isEqualTo(otherSummary);
    assertThat(result.latestMessage()).isEqualTo(lastMessageDto);
  }

  @Test
  void 최신_메시지가_없으면_latestMessage는_null이다() {
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);

    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "상대방", null));
    given(directMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId))
        .willReturn(Optional.empty());

    ConversationDto result = conversationMapper.toDto(conversation, myUserId, participants);

    assertThat(result.latestMessage()).isNull();
  }

  @Test
  void 본인만_참여자인_경우_with는_null이고_hasUnread는_false다() {
    List<ConversationParticipant> participants = List.of(myParticipant);

    given(directMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId))
        .willReturn(Optional.empty());

    ConversationDto result = conversationMapper.toDto(conversation, myUserId, participants);

    assertThat(result.with()).isNull();
    assertThat(result.hasUnread()).isFalse();
  }

  @Test
  void lastReadAt이_null이면_대화방에_메시지_존재여부로_hasUnread를_판단한다() {
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);

    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "상대방", null));
    given(directMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId))
        .willReturn(Optional.empty());
    given(directMessageRepository.existsByConversationId(conversationId)).willReturn(true);

    ConversationDto result = conversationMapper.toDto(conversation, myUserId, participants);

    assertThat(result.hasUnread()).isTrue();
    verify(directMessageRepository).existsByConversationId(conversationId);
  }

  @Test
  void lastReadAt_이후_상대방이_보낸_메시지가_있으면_hasUnread는_true다() {
    myParticipant.updateLastReadAt();
    List<ConversationParticipant> participants = List.of(myParticipant, otherParticipant);

    given(userService.getUserSummary(otherUserId))
        .willReturn(new UserSummary(otherUserId, "상대방", null));
    given(directMessageRepository.findTopByConversationIdOrderBySentAtDesc(conversationId))
        .willReturn(Optional.empty());
    given(directMessageRepository.existsByConversationIdAndSenderIdNotAndSentAtAfter(
        eq(conversationId), eq(myUserId), any(Instant.class)))
        .willReturn(true);

    ConversationDto result = conversationMapper.toDto(conversation, myUserId, participants);

    assertThat(result.hasUnread()).isTrue();
    verify(directMessageRepository).existsByConversationIdAndSenderIdNotAndSentAtAfter(
        eq(conversationId), eq(myUserId), any(Instant.class));
  }
}
