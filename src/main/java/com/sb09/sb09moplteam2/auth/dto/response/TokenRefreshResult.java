package com.sb09.sb09moplteam2.auth.dto.response;

import com.sb09.sb09moplteam2.user.dto.response.JwtDto;

public record TokenRefreshResult(
    JwtDto jwtDto,
    String refreshToken
) {}