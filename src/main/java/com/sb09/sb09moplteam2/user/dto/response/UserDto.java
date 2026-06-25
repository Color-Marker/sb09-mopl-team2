package com.sb09.sb09moplteam2.user.dto.response;

import com.sb09.sb09moplteam2.user.entity.Role;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

  private UUID id;
  private OffsetDateTime createdAt;
  private String email;
  private String name;
  private String profileImageUrl;
  private Role role;
  private boolean locked;
}
