package com.sb09.sb09moplteam2.exception.websocket;

import java.util.UUID;

public class ConversationParticipantNotFoundException extends RuntimeException {

  public ConversationParticipantNotFoundException(UUID conversationId, UUID userId) {
    super("ConversationParticipant를 찾을 수 없습니다. conversationId=" + conversationId + ", userId=" + userId);
  }
}
