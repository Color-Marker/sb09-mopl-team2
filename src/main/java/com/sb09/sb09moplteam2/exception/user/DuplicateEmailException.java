package com.sb09.sb09moplteam2.exception.user;

import com.sb09.sb09moplteam2.exception.ErrorCode;

public class DuplicateEmailException extends UserException {

  public DuplicateEmailException() {
    super(ErrorCode.DUPLICATE_USER);
  }

  public static DuplicateEmailException withEmail(String email) {
    DuplicateEmailException exception = new DuplicateEmailException();
    exception.addDetail("email", email);
    return exception;
  }
}