package com.sb09.sb09moplteam2.exception.follow;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class FollowNotFoundException extends MoplException {
  public FollowNotFoundException() { super(ErrorCode.FOLLOW_NOT_FOUND); }
}
