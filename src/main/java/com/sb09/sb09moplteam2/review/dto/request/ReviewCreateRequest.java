package com.sb09.sb09moplteam2.review.dto.request;

import java.util.UUID;

public record ReviewCreateRequest(
   UUID contentId,
   String text,
   Double rating
) {}
