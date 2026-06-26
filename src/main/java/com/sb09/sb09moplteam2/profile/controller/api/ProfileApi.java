package com.sb09.sb09moplteam2.profile.controller.api;

import com.sb09.sb09moplteam2.profile.dto.data.UserSummary;
import com.sb09.sb09moplteam2.profile.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.profile.dto.response.CursorResponseWatchingSessionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "프로필 관리", description = "사용자 프로필 및 시청 세션 관련 API")
public interface ProfileApi {

  @Operation(summary = "사용자 상세 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
  @GetMapping("/api/users/{userId}")
  ResponseEntity<UserSummary> getUserProfile(@PathVariable("userId") UUID userId);

  @Operation(summary = "프로필 변경", description = "본인의 프로필 정보(이름 등)를 수정합니다.")
  @PatchMapping("/api/users/{userId}")
  ResponseEntity<UserSummary> updateProfile(
      @PathVariable("userId") UUID userId,
      @RequestBody UserUpdateRequest request
  );

  @Operation(summary = "특정 사용자의 시청 세션 조회", description = "해당 사용자가 현재 시청 중인 콘텐츠 정보를 조회합니다.")
  @GetMapping("/api/users/{watcherId}/watching-sessions")
  ResponseEntity<CursorResponseWatchingSessionDto> getWatchingSessions(
      @PathVariable("watcherId") UUID watcherId
  );
}
