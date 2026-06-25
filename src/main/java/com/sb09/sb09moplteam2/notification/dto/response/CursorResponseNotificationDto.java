package com.sb09.sb09moplteam2.notification.dto.response;

import com.sb09.sb09moplteam2.common.SortDirection;
import java.util.List;
import java.util.UUID;

public record CursorResponseNotificationDto<T>(
    List<T> data,
    // 정렬 기준 값
    String nextCursor,
    // 동일 정렬값일 경우
    UUID nextIdAfter,
    boolean hasNext,
    Long totalCount,
    // createdAt 고정
    String sortBy,
    SortDirection sortDirection
) {

}
