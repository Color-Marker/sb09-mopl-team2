package com.sb09.sb09moplteam2.exception.websocket;

import java.util.UUID;

public class DirectMessageNotFoundException extends RuntimeException {
  public DirectMessageNotFoundException(UUID id) {
    super("DirectMessage를 찾을 수 없습니다. id=" + id);
  }
}
