package com.sb09.sb09moplteam2.dto;

import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.List;
import java.util.UUID;

public record ContentSummary(
    UUID id,
    ContentType type,
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags,
    double averageRating,
    int reviewCount,
    long watcherCount
) {}
