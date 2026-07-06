package com.sb09.sb09moplteam2.review.dto.response;

import com.sb09.sb09moplteam2.review.dto.data.ReviewDto;
import java.util.List;
import java.util.UUID;

public record CursorResponseReviewDto(
    List<ReviewDto> data,
    String nextCursor,
    UUID nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {}
