package com.sb09.sb09moplteam2.exception.follow;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class FollowForbiddenException extends MoplException {
  public FollowForbiddenException() { super(ErrorCode.FOLLOW_FORBIDDEN); }
}
