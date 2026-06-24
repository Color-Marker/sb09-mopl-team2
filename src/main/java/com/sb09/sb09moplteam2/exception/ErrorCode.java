package com.sb09.sb09moplteam2.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  // User 관련 에러 코드
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  DUPLICATE_USER("이미 존재하는 사용자입니다."),
  INVALID_USER_CREDENTIALS("잘못된 사용자 인증 정보입니다."),

  // Server 에러 코드
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
  INVALID_REQUEST("잘못된 요청입니다."),

  // Security 관련 에러 코드
  INVALID_TOKEN("토큰이 유효하지 않습니다."),
  INVALID_USER_DETAILS("사용자 인증 정보(UserDetails)가 유효하지 않습니다."),


  // notification 에러 코드
  NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다."),
  NOTIFICATION_FORBIDDEN("알림을 삭제할 권한이 없습니다.");

  private final String message;

  ErrorCode(String message){
    this.message = message;
  }
}
