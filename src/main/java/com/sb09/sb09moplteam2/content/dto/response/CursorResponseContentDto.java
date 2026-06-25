package com.sb09.sb09moplteam2.content.dto.response;

import com.sb09.sb09moplteam2.content.dto.data.ContentSummary;
import java.util.List;
import java.util.UUID;

public record CursorResponseContentDto(
   List<ContentSummary> data,
   String nextCursor,
   UUID nextIdAfter,
   Boolean hasNext,
   Long totalCount,
   String sortBy,
   String sortDirection
) {}
