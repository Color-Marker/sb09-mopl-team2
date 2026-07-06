package com.sb09.sb09moplteam2.exception.websocket;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import java.util.UUID;

public class ConversationParticipantNotFoundException extends MoplException {

  public ConversationParticipantNotFoundException(UUID conversationId, UUID userId) {
    super(ErrorCode.CONVERSATION_PARTICIPANT_NOT_FOUND);
    addDetail("conversationId", conversationId);
    addDetail("userId", userId);
  }
}
