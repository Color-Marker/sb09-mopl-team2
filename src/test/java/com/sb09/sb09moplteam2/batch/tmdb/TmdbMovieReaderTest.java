package com.sb09.sb09moplteam2.batch.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbClient;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieReader;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbPageResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmdbMovieReaderTest {

  @Mock
  private TmdbClient tmdbClient;

  @Test
  @DisplayName("movie 데이터를 순서대로 읽는다")
  void read_movie_데이터를_순서대로_읽는다() {
    TmdbEventResponse item1 = new TmdbEventResponse(1L, "영화1", null, "줄거리1", null, null);
    TmdbEventResponse item2 = new TmdbEventResponse(2L, "영화2", null, "줄거리2", null, null);
    TmdbPageResponse<TmdbEventResponse> page = new TmdbPageResponse<>(List.of(item1, item2), 1, 1);
//                                                                  results              page totalPages
    given(tmdbClient.fetchMovies(1)).willReturn(page);

    TmdbMovieReader reader = new TmdbMovieReader(tmdbClient, ContentType.movie);

    TmdbEventResponse result1 = reader.read();
    TmdbEventResponse result2 = reader.read();
    TmdbEventResponse result3 = reader.read();

    assertThat(result1.title()).isEqualTo("영화1");
    assertThat(result2.title()).isEqualTo("영화2");
    assertThat(result3).isNull();
  }

  @Test
  @DisplayName("tvSeries 데이터를 순서대로 읽는다")
  void read_tvSeries_데이터를_순서대로_읽는다() {
    TmdbEventResponse item = new TmdbEventResponse(1L, null, "드라마1", "줄거리", null, null);
    TmdbPageResponse<TmdbEventResponse> page = new TmdbPageResponse<>(List.of(item), 1, 1);

    given(tmdbClient.fetchTvSeries(1)).willReturn(page);

    TmdbMovieReader reader = new TmdbMovieReader(tmdbClient, ContentType.tvSeries);

    TmdbEventResponse result = reader.read();

    assertThat(result.name()).isEqualTo("드라마1");
  }
}