package com.sb09.sb09moplteam2.exception.notification;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import java.util.UUID;

public class NotificationForbiddenException extends NotificationException {

  public NotificationForbiddenException() {
    super(ErrorCode.NOTIFICATION_FORBIDDEN);
  }

  public static NotificationForbiddenException withId(UUID notificationId, UUID receiverId) {
    NotificationForbiddenException exception = new NotificationForbiddenException();
    exception.addDetail("notificationId", notificationId);
    exception.addDetail("receiverId", receiverId);
    return exception;
  }
}