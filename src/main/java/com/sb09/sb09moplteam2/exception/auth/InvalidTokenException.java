package com.sb09.sb09moplteam2.exception.auth;

import com.sb09.sb09moplteam2.exception.ErrorCode;

public class InvalidTokenException extends AuthException {
  public InvalidTokenException() {
    super(ErrorCode.INVALID_TOKEN);
  }
}