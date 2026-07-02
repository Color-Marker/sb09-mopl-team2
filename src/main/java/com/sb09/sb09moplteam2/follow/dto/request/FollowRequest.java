package com.sb09.sb09moplteam2.follow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowRequest {

  @NotNull(message = "팔로우할 대상의 ID는 필수입니다.")
  private UUID followeeId;

  public FollowRequest(UUID followeeId) {
    this.followeeId = followeeId;
  }
}
