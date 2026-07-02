package com.sb09.sb09moplteam2.exception.follow;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class CannotFollowSelfException extends MoplException {

  public CannotFollowSelfException() {
    super(ErrorCode.CANNOT_FOLLOW_SELF);
  }
}
