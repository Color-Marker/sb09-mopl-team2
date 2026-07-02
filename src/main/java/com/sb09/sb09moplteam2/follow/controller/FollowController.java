package com.sb09.sb09moplteam2.follow.controller;

import com.sb09.sb09moplteam2.follow.dto.data.FollowDto;
import com.sb09.sb09moplteam2.follow.dto.request.FollowRequest;
import com.sb09.sb09moplteam2.follow.service.FollowService;
import com.sb09.sb09moplteam2.security.jwt.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

  private final FollowService followService;

  // 1. 팔로우 하기 (POST /api/follows)
  @PostMapping
  public ResponseEntity<FollowDto> follow(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody FollowRequest request) {
    FollowDto response = followService.follow(userDetails.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 2. 팔로우 취소 (DELETE /api/follows/{followId})
  @DeleteMapping("/{followId}")
  public ResponseEntity<Void> unfollow(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID followId) {
    followService.unfollow(userDetails.getId(), followId);
    return ResponseEntity.noContent().build();
  }

  // 3. 팔로우 여부 조회 (GET /api/follows/followed-by-me?followeeId={uuid})
  @GetMapping("/followed-by-me")
  public ResponseEntity<FollowDto> checkFollowingStatus(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam UUID followeeId) {
    FollowDto response = followService.getFollowDetails(userDetails.getId(), followeeId);
    return ResponseEntity.ok(response);
  }

  // 4. 특정 유저의 팔로워 수 조회 (GET /api/follows/count?followeeId={uuid})
  @GetMapping("/count")
  public ResponseEntity<Long> getFollowerCount(
      @RequestParam UUID followeeId) {
    long count = followService.getFollowerCount(followeeId);
    return ResponseEntity.ok(count);
  }
}
