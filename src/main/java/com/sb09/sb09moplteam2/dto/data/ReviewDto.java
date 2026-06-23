package com.sb09.sb09moplteam2.dto.data;

import java.util.UUID;

public record ReviewDto(
    UUID id,
    UUID contentId,
    UserSummary author,
    String text,
    Double rating
) {}