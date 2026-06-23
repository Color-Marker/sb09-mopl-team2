package com.sb09.sb09moplteam2.dto.request.content;

import java.util.List;

public record ContentCreateRequest(
   String type,
   String title,
   String description,
   List<String> tags
) {}
