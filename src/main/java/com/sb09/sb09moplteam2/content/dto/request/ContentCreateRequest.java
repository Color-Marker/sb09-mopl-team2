package com.sb09.sb09moplteam2.content.dto.request;

import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.List;

public record ContentCreateRequest(
    ContentType type,
    String title,
    String description,
    List<String> tags
) {}
