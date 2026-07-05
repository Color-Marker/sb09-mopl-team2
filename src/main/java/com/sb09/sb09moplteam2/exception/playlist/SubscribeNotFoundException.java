package com.sb09.sb09moplteam2.exception.playlist;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class SubscribeNotFoundException extends MoplException {

  public SubscribeNotFoundException() {
    super(ErrorCode.SUBSCRIBE_NOT_FOUND);
  }
}
