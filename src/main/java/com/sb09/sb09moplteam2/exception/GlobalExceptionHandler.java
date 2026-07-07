package com.sb09.sb09moplteam2.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e){
    log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }

  @ExceptionHandler(MoplException.class)
  public ResponseEntity<ErrorResponse> handleMoplException(MoplException exception) {
    log.error("커스텀 예외 발생: code={}, message={}", exception.getErrorCode(), exception.getMessage(), exception);
    HttpStatus status = exception.getErrorCode().getHttpStatus();
    ErrorResponse response = new ErrorResponse(exception);
    return ResponseEntity
        .status(status)
        .body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    log.error("요청 유효성 검사 실패: {}", ex.getMessage());
    Map<String, Object> validationErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });
    ErrorResponse response = new ErrorResponse(
        ex.getClass().getSimpleName(),
        "요청 데이터 유효성 검사에 실패했습니다",
        validationErrors
    );
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
    log.error("권한 거부 오류 발생: {}", ex.getMessage());
    ErrorResponse response = new ErrorResponse(
        ex.getClass().getSimpleName(),
        "요청에 대한 권한이 없습니다",
        new HashMap<>()
    );
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
    log.error("요청 파라미터 유효성 검사 실패: {}", ex.getMessage());
    Map<String, Object> validationErrors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation -> {
      // "findByContent.limit" 같은 형태에서 파라미터명만 추출
      String field = violation.getPropertyPath().toString();
      field = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
      validationErrors.put(field, violation.getMessage());
    });
    ErrorResponse response = new ErrorResponse(
        ex.getClass().getSimpleName(),
        "요청 파라미터 유효성 검사에 실패했습니다",
        validationErrors
    );
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex) {
    log.error("필수 요청 파라미터 누락: {}", ex.getMessage());
    Map<String, Object> validationErrors = new HashMap<>();
    validationErrors.put(ex.getParameterName(), "필수 파라미터가 누락되었습니다");
    ErrorResponse response = new ErrorResponse(
        ex.getClass().getSimpleName(),
        "요청에 필수 파라미터가 누락되었습니다",
        validationErrors
    );
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    log.error("요청 파라미터 타입 변환 실패: {}", ex.getMessage());
    Map<String, Object> validationErrors = new HashMap<>();
    String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음";
    validationErrors.put(ex.getName(), String.format("%s 타입으로 변환할 수 없는 값입니다", requiredType));
    ErrorResponse response = new ErrorResponse(
        ex.getClass().getSimpleName(),
        "요청 파라미터 형식이 올바르지 않습니다",
        validationErrors
    );
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

}
