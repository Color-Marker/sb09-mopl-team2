package com.sb09.sb09moplteam2.exception.auth;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class AuthException extends MoplException {
  public AuthException(ErrorCode errorCode) {
    super(errorCode);
  }
}