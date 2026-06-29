package com.sb09.sb09moplteam2.user.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 2, max = 30, message = "이름은 최소 2자 이상이어야 합니다")
    String name
) {}