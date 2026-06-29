package com.sb09.sb09moplteam2.playlist.dto.response;

import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import java.util.List;
import java.util.UUID;

public record CursorResponsePlaylistDto(
    List<PlaylistDto> data,
    String nextCursor,
    UUID nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

}
