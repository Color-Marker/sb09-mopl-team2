package com.sb09.sb09moplteam2.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserLockUpdateRequest(
    @NotNull(message = "잠금 상태를 입력해주세요")
    Boolean locked
) {}