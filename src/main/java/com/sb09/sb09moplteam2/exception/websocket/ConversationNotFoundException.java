package com.sb09.sb09moplteam2.exception.websocket;

import java.util.UUID;

public class ConversationNotFoundException extends RuntimeException {
  public ConversationNotFoundException(UUID id) {
    super("Conversation을 찾을 수 없습니다. id=" + id);
  }
}
