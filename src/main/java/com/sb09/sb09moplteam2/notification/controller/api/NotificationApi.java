package com.sb09.sb09moplteam2.notification.controller.api;

import com.sb09.sb09moplteam2.notification.dto.NotificationDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface NotificationApi {

  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "알림 목록 조회 성공",
          content =
      ),
      @ApiResponse(
          responseCode = "400", description = "잘못된 요청",
          content =
      ),
      @ApiResponse(
          responseCode = "401", description = "인증되지 않은 요청",
          content =
      ),
      @ApiResponse(
          responseCode = "500", description = "서버 내부 오류",
          content =
      )
  })
  ResponseEntity<List<NotificationDto>> findAllByReceiverId(
      @Parameter(hidden = true) DiscodeitUserDetails principal
  );

  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204", description = "알림 삭제 성공",
          content = @Content(schema = @Schema(hidden = true))
      ),
      @ApiResponse(
          responseCode = "401", description = "인증되지 않은 요청",
          content = @Content(schema = @Schema(hidden = true))
      ),
      @ApiResponse(
          responseCode = "404", description = "알림을 찾을 수 없음",
          content = @Content(schema = @Schema(hidden = true))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(hidden = true) DiscodeitUserDetails principal,
      @Parameter(description = "알림 ID") UUID notificationId
  );

}
