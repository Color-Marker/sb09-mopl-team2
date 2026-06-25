package com.sb09.sb09moplteam2.notification.controller;

import com.sb09.sb09moplteam2.notification.controller.api.NotificationApi;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListdRequest;
import com.sb09.sb09moplteam2.notification.dto.response.CursorResponseNotificationDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @GetMapping
  @Override
  public ResponseEntity<CursorResponseNotificationDto<NotificationDto>> findAllByUserId(
      @AuthenticationPrincipal MoplUserDetails principal,
      NotificationListdRequest request
  ) {
    UUID userId = principal.getUserDto().id();
    log.info("알림 목록 조회 요청: userId={}", userId);
    CursorResponseNotificationDto<NotificationDto> result = notificationService.findAllByUserId(userId);
    log.debug("알림 목록 조회 응답: count={}", result.totalCount());
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{notificationId}")
  @Override
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal MoplUserDetails principal,
      @PathVariable UUID notificationId
  ) {
    UUID userId = principal.getUserDto().id();
    log.info("알림 삭제 요청: id={}, userId={}", notificationId, userId);
    notificationService.delete(notificationId, userId);
    log.debug("알림 삭제 응답: id={}", notificationId);
    return ResponseEntity.noContent().build();
  }
}
