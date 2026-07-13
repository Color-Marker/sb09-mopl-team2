package com.sb09.sb09moplteam2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // User 관련 에러 코드
  USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.BAD_REQUEST),
  INVALID_USER_CREDENTIALS("잘못된 사용자 인증 정보입니다.", HttpStatus.UNAUTHORIZED),
  USER_FORBIDDEN("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // Server 에러 코드
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),

  // Security 관련 에러 코드
  INVALID_TOKEN("토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
  INVALID_USER_DETAILS("사용자 인증 정보(UserDetails)가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),

  // notification 에러 코드
  NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOTIFICATION_FORBIDDEN("알림을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // Follow 에러 코드
  FOLLOW_NOT_FOUND("존재하지 않는 팔로우 내역입니다.", HttpStatus.NOT_FOUND),
  ALREADY_FOLLOWING("이미 팔로우 중인 사용자입니다.", HttpStatus.CONFLICT),
  SELF_FOLLOW_NOT_ALLOWED("자기 자신을 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST),
  FOLLOW_FORBIDDEN("해당 팔로우 내역에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // review 에러 코드
  REVIEW_NOT_FOUND("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  REVIEW_FORBIDDEN("리뷰에 대해 권한이 없습니다.", HttpStatus.FORBIDDEN),
  DUPLICATE_REVIEW("이미 리뷰를 작성했습니다.", HttpStatus.CONFLICT),

  // playlist 에러 코드
  PLAYLIST_NOT_FOUND("플레이리스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  PLAYLIST_FORBIDDEN("플레이리스트 삭제 권한이 없습니다.",HttpStatus.FORBIDDEN),
  DUPLICATE_SUBSCRIBE("이미 구독 중입니다", HttpStatus.CONFLICT),
  SUBSCRIBE_NOT_FOUND("구독 중이 아닙니다", HttpStatus.NOT_FOUND),

  // Conversation 에러 코드
  CONVERSATION_NOT_FOUND("존재하지 않는 대화방입니다.", HttpStatus.NOT_FOUND),
  CONVERSATION_PARTICIPANT_NOT_FOUND("해당 대화방의 참여자가 아닙니다.", HttpStatus.FORBIDDEN),
  CONVERSATION_PARTICIPANT_ALREADY_EXISTS("이미 참여 중인 대화방입니다.", HttpStatus.CONFLICT),
  DIRECT_MESSAGE_NOT_FOUND("존재하지 않는 메시지입니다.", HttpStatus.NOT_FOUND),
  WATCHING_SESSION_NOT_FOUND("존재하지 않는 시청 세션입니다.", HttpStatus.NOT_FOUND),


  // content 에러 코드
  CONTENT_NOT_FOUND("콘텐츠를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
  DUPLICATE_CONTENT("이미 추가된 콘텐츠입니다", HttpStatus.CONFLICT),
  CONTENT_FORBIDDEN("", HttpStatus.FORBIDDEN);

  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String message, HttpStatus httpStatus){
    this.message = message;
    this.httpStatus = httpStatus;
  }
}
