package com.sb09.sb09moplteam2.profile.controller;

import com.sb09.sb09moplteam2.profile.controller.api.ConversationApi;
import com.sb09.sb09moplteam2.profile.dto.data.ConversationDto;
import com.sb09.sb09moplteam2.profile.dto.data.DirectMessageDto;
import com.sb09.sb09moplteam2.profile.dto.data.UserSummary;
import com.sb09.sb09moplteam2.profile.dto.request.ConversationCreateRequest;
import com.sb09.sb09moplteam2.profile.dto.response.CursorResponseConversationDto;
import com.sb09.sb09moplteam2.profile.dto.response.CursorResponseDirectMessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class ConversationController implements ConversationApi {

  private final UserSummary mockMe = new UserSummary(UUID.randomUUID(), "나", "url");
  private final UserSummary mockOther = new UserSummary(UUID.randomUUID(), "상대방", "url");

  @Override
  public ResponseEntity<CursorResponseConversationDto> getConversations(String cursor) {
    UUID conversationId = UUID.randomUUID();
    DirectMessageDto lastMessage = new DirectMessageDto(
        UUID.randomUUID(), conversationId, LocalDateTime.now(),
        mockOther, mockMe, "프로젝트 화이팅입니다!"
    );

    ConversationDto room = new ConversationDto(conversationId, mockOther, lastMessage, true);
    CursorResponseConversationDto response = new CursorResponseConversationDto(
        List.of(room), "cursor", UUID.randomUUID(), false, 1L, "createdAt", "DESC"
    );
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ConversationDto> createConversation(ConversationCreateRequest request) {
    ConversationDto newRoom = new ConversationDto(
        UUID.randomUUID(), mockOther, null, false
    );
    return ResponseEntity.ok(newRoom);
  }

  @Override
  public ResponseEntity<ConversationDto> getConversationWithUser(UUID userId) {
    ConversationDto existingRoom = new ConversationDto(
        UUID.randomUUID(), mockOther, null, false
    );
    return ResponseEntity.ok(existingRoom);
  }

  @Override
  public ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(UUID conversationId, String cursor) {
    DirectMessageDto msg1 = new DirectMessageDto(
        UUID.randomUUID(), conversationId, LocalDateTime.now().minusMinutes(5),
        mockOther, mockMe, "안녕하세요!"
    );
    DirectMessageDto msg2 = new DirectMessageDto(
        UUID.randomUUID(), conversationId, LocalDateTime.now(),
        mockMe, mockOther, "네, 반갑습니다~"
    );

    CursorResponseDirectMessageDto response = new CursorResponseDirectMessageDto(
        List.of(msg1, msg2), null, null, false, 2L, "createdAt", "ASC"
    );
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> readDirectMessage(UUID conversationId, UUID directMessageId) {
    return ResponseEntity.ok().build();
  }
}
