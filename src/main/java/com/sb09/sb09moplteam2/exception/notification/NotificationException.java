package com.sb09.sb09moplteam2.exception.notification;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class NotificationException extends MoplException {
  public NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }
}
