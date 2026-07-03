package com.sb09.sb09moplteam2.exception.content;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class ContentNotFoundException extends MoplException {

  public ContentNotFoundException() {
    super(ErrorCode.CONTENT_NOT_FOUND);
  }
}
