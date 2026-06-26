package com.sb09.sb09moplteam2.exception.websocket;

import java.util.UUID;

public class WatchingSessionNotFoundException extends RuntimeException {
  public WatchingSessionNotFoundException(UUID id) {
    super("WatchingSession을 찾을 수 없습니다. id=" + id);
  }
}
