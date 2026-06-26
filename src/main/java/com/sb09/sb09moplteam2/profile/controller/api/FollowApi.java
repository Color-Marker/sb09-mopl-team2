package com.sb09.sb09moplteam2.profile.controller.api;

import com.sb09.sb09moplteam2.profile.dto.data.FollowDto;
import com.sb09.sb09moplteam2.profile.dto.request.FollowRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "팔로우 관리", description = "사용자 간의 팔로우/언팔로우 관련 API")
public interface FollowApi {

  @Operation(summary = "팔로우", description = "다른 사용자를 팔로우합니다.")
  @PostMapping("/api/follows")
  ResponseEntity<FollowDto> createFollow(@RequestBody FollowRequest request);

  @Operation(summary = "팔로우 취소", description = "특정 팔로우 관계를 취소(삭제)합니다.")
  @DeleteMapping("/api/follows/{followId}")
  ResponseEntity<Void> deleteFollow(@PathVariable("followId") UUID followId);

  @Operation(summary = "내가 팔로우하는지 여부 조회", description = "특정 유저를 내가 팔로우하고 있는지 상태를 확인합니다.")
  @GetMapping("/api/follows/followed-by-me")
  ResponseEntity<Map<String, Boolean>> checkFollowedByMe(
      @RequestParam("targetUserId") UUID targetUserId
  );

  @Operation(summary = "특정 유저의 팔로워 수 조회", description = "해당 유저를 팔로우하는 사람의 수를 반환합니다.")
  @GetMapping("/api/follows/count")
  ResponseEntity<Map<String, Long>> getFollowerCount(
      @RequestParam("targetUserId") UUID targetUserId
  );
}
