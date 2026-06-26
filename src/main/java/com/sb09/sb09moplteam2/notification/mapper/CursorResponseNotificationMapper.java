package com.sb09.sb09moplteam2.notification.mapper;

import com.sb09.sb09moplteam2.common.SortDirection;
import com.sb09.sb09moplteam2.dto.CursorResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class CursorResponseNotificationMapper {
  public <T, R> CursorResponse<R> fromSlice(
      Slice<T> slice,
      Function<T, R> converter,
      Function<T, Instant> cursorExtractor,
      Function<T, UUID> idAfterExtractor,
      Long totalCount,
      SortDirection sortDirection
  ){
    List<R> dtos = slice.getContent().stream()
        .map(converter)
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;

    if (slice.hasNext() && !slice.getContent().isEmpty()) {
      T last = slice.getContent().get(slice.getContent().size() - 1);
      nextCursor = cursorExtractor.apply(last).toString();
      nextIdAfter = idAfterExtractor.apply(last);
    }
    return new CursorResponse<>(
        dtos,
        nextCursor,
        nextIdAfter,
        slice.hasNext(),
        totalCount,
        "createdAt",
        sortDirection.toString()
    );
  }
}
