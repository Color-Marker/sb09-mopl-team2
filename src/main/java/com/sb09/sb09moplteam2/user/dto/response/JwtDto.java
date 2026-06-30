package com.sb09.sb09moplteam2.user.dto.response;

import com.sb09.sb09moplteam2.user.dto.data.UserDto;

public record JwtDto(
    UserDto userDto,
    String accessToken
) {}