package com.sb09.sb09moplteam2.notification.dto;

import com.sb09.sb09moplteam2.common.SortDirection;
import java.util.List;
import java.util.UUID;

public record CursorResponseNotificationDto<T>(
    List<T> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String sortBy,
    SortDirection sortDirection
) {

}
