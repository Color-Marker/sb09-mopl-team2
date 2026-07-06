package com.sb09.sb09moplteam2.exception.websocket;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import java.util.UUID;

public class ConversationNotFoundException extends MoplException {

  public ConversationNotFoundException(UUID id) {
    super(ErrorCode.CONVERSATION_NOT_FOUND);
    addDetail("id", id);
  }
}
