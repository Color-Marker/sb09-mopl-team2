package com.sb09.sb09moplteam2.exception.follow;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class AlreadyFollowingException extends MoplException {
  public AlreadyFollowingException() { super(ErrorCode.ALREADY_FOLLOWING); }
}
