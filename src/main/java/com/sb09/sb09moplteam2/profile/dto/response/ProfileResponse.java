package com.sb09.sb09moplteam2.profile.dto.response;

import com.sb09.sb09moplteam2.user.entity.User;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    String name,
    String email,
    String profileImageUrl
) {
  public static ProfileResponse from(User user) {
    return new ProfileResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getProfileImageUrl()
    );
  }
}
