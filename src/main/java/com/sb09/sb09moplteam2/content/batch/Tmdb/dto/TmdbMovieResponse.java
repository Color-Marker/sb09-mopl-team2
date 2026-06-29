package com.sb09.sb09moplteam2.content.batch.Tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieResponse(
    Long id,
    String title,
    String name,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("release_date") String releaseDate
) {

}
