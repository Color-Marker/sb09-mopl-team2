package com.sb09.sb09moplteam2.user.dto.data;

import com.sb09.sb09moplteam2.user.entity.Role;
import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    boolean locked
) {}