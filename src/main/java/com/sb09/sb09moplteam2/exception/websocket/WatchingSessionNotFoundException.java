package com.sb09.sb09moplteam2.exception.websocket;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import java.util.UUID;

public class WatchingSessionNotFoundException extends MoplException {

  public WatchingSessionNotFoundException(UUID id) {
    super(ErrorCode.WATCHING_SESSION_NOT_FOUND);
    addDetail("id", id);
  }
}
