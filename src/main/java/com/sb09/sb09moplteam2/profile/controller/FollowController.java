package com.sb09.sb09moplteam2.profile.controller;

import com.sb09.sb09moplteam2.profile.controller.api.FollowApi;
import com.sb09.sb09moplteam2.profile.dto.data.FollowDto;
import com.sb09.sb09moplteam2.profile.dto.request.FollowRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class FollowController implements FollowApi {

  @Override
  public ResponseEntity<FollowDto> createFollow(FollowRequest request) {
    FollowDto mockFollow = new FollowDto(
        UUID.randomUUID(),
        request.followeeId(),
        UUID.randomUUID()
    );
    return ResponseEntity.ok(mockFollow);
  }

  @Override
  public ResponseEntity<Void> deleteFollow(UUID followId) {
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Map<String, Boolean>> checkFollowedByMe(UUID targetUserId) {
    return ResponseEntity.ok(Map.of("isFollowed", true));
  }

  @Override
  public ResponseEntity<Map<String, Long>> getFollowerCount(UUID targetUserId) {
    return ResponseEntity.ok(Map.of("followerCount", 1234L));
  }
}
