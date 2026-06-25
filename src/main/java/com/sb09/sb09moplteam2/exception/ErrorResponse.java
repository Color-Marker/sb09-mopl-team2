package com.sb09.sb09moplteam2.exception;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  private String exceptionName;
  private String message;
  private Map<String, String> details;
}