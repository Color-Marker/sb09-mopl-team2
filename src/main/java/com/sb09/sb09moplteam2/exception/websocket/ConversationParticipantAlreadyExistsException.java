package com.sb09.sb09moplteam2.exception.websocket;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import java.util.UUID;

public class ConversationParticipantAlreadyExistsException extends MoplException {

  public ConversationParticipantAlreadyExistsException(UUID conversationId, UUID userId) {
    super(ErrorCode.CONVERSATION_PARTICIPANT_ALREADY_EXISTS);
    addDetail("conversationId", conversationId);
    addDetail("userId", userId);
  }
}
