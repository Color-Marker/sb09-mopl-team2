package com.sb09.sb09moplteam2.content.batch.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbEventResponse(
    Long id,
    String title,
    String name,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("release_date") String releaseDate,
    @JsonProperty("genre_ids") List<Integer> genreIds
) {

}
