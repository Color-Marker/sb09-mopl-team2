package com.sb09.sb09moplteam2.profile.dto.data;

import java.util.UUID;

public record FollowDto(
    UUID id,
    UUID followeeId,
    UUID followerId
) {}
