package com.sb09.sb09moplteam2.exception.follow;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class SelfFollowNotAllowedException extends MoplException {
  public SelfFollowNotAllowedException() { super(ErrorCode.SELF_FOLLOW_NOT_ALLOWED); }
}
