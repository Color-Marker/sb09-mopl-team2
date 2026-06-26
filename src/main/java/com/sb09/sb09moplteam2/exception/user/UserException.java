package com.sb09.sb09moplteam2.exception.user;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class UserException extends MoplException {
  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }
}