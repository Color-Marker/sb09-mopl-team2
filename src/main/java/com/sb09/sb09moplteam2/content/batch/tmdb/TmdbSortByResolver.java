package com.sb09.sb09moplteam2.content.batch.tmdb;

import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.time.LocalDate;
import java.util.List;

public class TmdbSortByResolver {

  private static final List<String> MOVIE_SORT_OPTIONS = List.of(
      "popularity.desc",
      "primary_release_date.desc",
      "vote_average.desc"
  );

  private static final List<String> TV_SORT_OPTIONS = List.of(
      "popularity.desc",
      "first_air_date.desc",
      "vote_average.desc"
  );

  private TmdbSortByResolver() {}

  public static String resolveTodaySortBy(ContentType contentType) {
    List<String> options = contentType == ContentType.movie ? MOVIE_SORT_OPTIONS : TV_SORT_OPTIONS;
    int index = (int) (LocalDate.now().toEpochDay() % options.size());
    return options.get(index);
  }
}