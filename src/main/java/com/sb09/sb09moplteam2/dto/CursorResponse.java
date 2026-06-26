package com.sb09.sb09moplteam2.dto;

import com.sb09.sb09moplteam2.common.SortDirection;
import java.util.List;
import java.util.UUID;

public record CursorResponse<T>(
    List<T> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) {}
