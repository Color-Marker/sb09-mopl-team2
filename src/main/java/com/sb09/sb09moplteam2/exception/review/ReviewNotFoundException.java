package com.sb09.sb09moplteam2.exception.review;

import com.sb09.sb09moplteam2.exception.ErrorCode;

public class ReviewNotFoundException extends ReviewException {

  public ReviewNotFoundException() {
    super(ErrorCode.REVIEW_NOT_FOUND);
  }
}
