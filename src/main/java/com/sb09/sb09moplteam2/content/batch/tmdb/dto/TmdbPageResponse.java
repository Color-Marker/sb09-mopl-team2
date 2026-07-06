package com.sb09.sb09moplteam2.content.batch.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbPageResponse<T>(
    List<T> results,
    int page,
    @JsonProperty("total_pages") int totalPages
) {
}
