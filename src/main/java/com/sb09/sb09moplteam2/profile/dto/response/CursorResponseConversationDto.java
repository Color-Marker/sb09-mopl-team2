package com.sb09.sb09moplteam2.profile.dto.response;

import com.sb09.sb09moplteam2.profile.dto.data.ConversationDto;
import java.util.List;
import java.util.UUID;

public record CursorResponseConversationDto(
    List<ConversationDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {}
