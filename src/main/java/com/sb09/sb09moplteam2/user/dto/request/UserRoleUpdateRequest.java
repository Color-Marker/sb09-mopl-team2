package com.sb09.sb09moplteam2.user.dto.request;

import com.sb09.sb09moplteam2.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
    @NotNull(message = "권한을 입력해주세요")
    Role role
) {}