package com.sb09.sb09moplteam2.profile.dto.data;

import java.util.UUID;

public record UserSummary(
    UUID userId,
    String name,
    String profileImageUrl
) {}
