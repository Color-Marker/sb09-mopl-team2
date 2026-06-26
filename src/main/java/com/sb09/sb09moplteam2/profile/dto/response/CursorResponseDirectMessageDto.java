package com.sb09.sb09moplteam2.profile.dto.response;

import com.sb09.sb09moplteam2.profile.dto.data.DirectMessageDto;
import java.util.List;
import java.util.UUID;

public record CursorResponseDirectMessageDto(
    List<DirectMessageDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {}
