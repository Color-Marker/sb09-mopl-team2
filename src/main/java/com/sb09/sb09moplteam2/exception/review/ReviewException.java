package com.sb09.sb09moplteam2.exception.review;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class ReviewException extends MoplException {

  public ReviewException(ErrorCode errorCode) {
    super(errorCode);
  }
}
