package com.sb09.sb09moplteam2.exception.profile;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class UserForbiddenException extends MoplException {

  public UserForbiddenException() {
    super(ErrorCode.USER_FORBIDDEN);
  }
}
