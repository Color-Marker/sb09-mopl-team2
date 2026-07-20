package com.sb09.sb09moplteam2.websocket.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.websocket.repository.ConversationParticipantRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DirectMessageMapperTest {

  @Mock
  private UserService userService;
  @Mock
  private ConversationParticipantRepository participantRepository;

  @InjectMocks
  private DirectMessageMapper directMessageMapper;

  private UUID conversationId;
  private UUID senderId;
  private UUID receiverId;
  private Conversation conversation;
  private ConversationParticipant senderParticipant;
  private ConversationParticipant receiverParticipant;

  @BeforeEach
  void setUp() {
    conversationId = UUID.randomUUID();
    senderId = UUID.randomUUID();
    receiverId = UUID.randomUUID();

    conversation = Conversation.createDirect();
    ReflectionTestUtils.setField(conversation, "id", conversationId);

    senderParticipant = ConversationParticipant.of(conversation, senderId);
    receiverParticipant = ConversationParticipant.of(conversation, receiverId);
  }

  @Test
  void 단건_조회용_toDto는_참여자를_직접_조회해서_위임한다() {
    DirectMessage message = DirectMessage.of(conversation, senderId, "안녕");
    List<ConversationParticipant> participants = List.of(senderParticipant, receiverParticipant);

    given(participantRepository.findByConversation(conversation)).willReturn(participants);
    given(userService.getUserSummary(senderId))
        .willReturn(new UserSummary(senderId, "보낸사람", null));
    given(userService.getUserSummary(receiverId))
        .willReturn(new UserSummary(receiverId, "받는사람", null));

    DirectMessageDto result = directMessageMapper.toDto(message);

    assertThat(result.conversationId()).isEqualTo(conversationId);
    verify(participantRepository).findByConversation(conversation);
  }

  @Test
  void sender와_receiver_정보를_포함한_DirectMessageDto를_생성한다() {
    DirectMessage message = DirectMessage.of(conversation, senderId, "안녕하세요");
    List<ConversationParticipant> participants = List.of(senderParticipant, receiverParticipant);
    UserSummary senderSummary = new UserSummary(senderId, "보낸사람", "sender.jpg");
    UserSummary receiverSummary = new UserSummary(receiverId, "받는사람", "receiver.jpg");

    given(userService.getUserSummary(senderId)).willReturn(senderSummary);
    given(userService.getUserSummary(receiverId)).willReturn(receiverSummary);

    DirectMessageDto result = directMessageMapper.toDto(message, participants);

    assertThat(result.id()).isEqualTo(message.getId());
    assertThat(result.conversationId()).isEqualTo(conversationId);
    assertThat(result.sender()).isEqualTo(senderSummary);
    assertThat(result.receiver()).isEqualTo(receiverSummary);
    assertThat(result.content()).isEqualTo("안녕하세요");
    assertThat(result.createdAt()).isEqualTo(message.getSentAt());
  }

  @Test
  void 상대방_참여자가_없으면_receiver는_null이다() {
    DirectMessage message = DirectMessage.of(conversation, senderId, "혼자");
    List<ConversationParticipant> participants = List.of(senderParticipant);

    given(userService.getUserSummary(senderId))
        .willReturn(new UserSummary(senderId, "보낸사람", null));

    DirectMessageDto result = directMessageMapper.toDto(message, participants);

    assertThat(result.receiver()).isNull();
  }
}
