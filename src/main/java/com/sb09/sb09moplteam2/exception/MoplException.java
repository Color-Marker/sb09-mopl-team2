package com.sb09.sb09moplteam2.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class MoplException extends RuntimeException{
  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public MoplException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public MoplException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public void addDetail(String key, Object value) {
    this.details.put(key, value);
  }

}
