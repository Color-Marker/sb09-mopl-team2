package com.sb09.sb09moplteam2.exception.websocket;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import java.util.UUID;

public class DirectMessageNotFoundException extends MoplException {

  public DirectMessageNotFoundException(UUID id) {
    super(ErrorCode.DIRECT_MESSAGE_NOT_FOUND);
    addDetail("id", id);
  }
}
