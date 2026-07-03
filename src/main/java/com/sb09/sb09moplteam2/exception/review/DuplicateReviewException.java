package com.sb09.sb09moplteam2.exception.review;

import com.sb09.sb09moplteam2.exception.ErrorCode;

public class DuplicateReviewException extends ReviewException {

  public DuplicateReviewException() {
    super(ErrorCode.DUPLICATE_REVIEW);
  }
}
