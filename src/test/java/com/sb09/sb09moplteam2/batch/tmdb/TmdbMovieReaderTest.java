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
    TmdbEventResponse item1 = new TmdbEventResponse(1L, "영화1", null, "줄거리1", null, null, List.of(28));
    TmdbEventResponse item2 = new TmdbEventResponse(2L, "영화2", null, "줄거리2", null, null, List.of(35));
    TmdbPageResponse<TmdbEventResponse> page = new TmdbPageResponse<>(List.of(item1, item2), 1, 1);
    given(tmdbClient.fetchMovies(1)).willReturn(page);

    TmdbMovieReader reader = new TmdbMovieReader(tmdbClient, ContentType.movie, 1, 1, "partition0");

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
    TmdbEventResponse item = new TmdbEventResponse(1L, null, "드라마1", "줄거리", null, null, List.of(18));
    TmdbPageResponse<TmdbEventResponse> page = new TmdbPageResponse<>(List.of(item), 1, 1);

    given(tmdbClient.fetchTvSeries(1)).willReturn(page);

    TmdbMovieReader reader = new TmdbMovieReader(tmdbClient, ContentType.tvSeries, 1, 1, "partition0");

    TmdbEventResponse result = reader.read();

    assertThat(result.name()).isEqualTo("드라마1");
  }

  @Test
  @DisplayName("startPage부터 endPage까지 여러 페이지를 순회한다")
  void read_startPage부터_endPage까지_여러_페이지를_읽는다() {
    TmdbEventResponse item1 = new TmdbEventResponse(1L, "영화1", null, "줄거리1", null, null, List.of(28));
    TmdbEventResponse item2 = new TmdbEventResponse(2L, "영화2", null, "줄거리2", null, null, List.of(35));
    TmdbPageResponse<TmdbEventResponse> page1 = new TmdbPageResponse<>(List.of(item1), 2, 2);
    TmdbPageResponse<TmdbEventResponse> page2 = new TmdbPageResponse<>(List.of(item2), 2, 2);

    given(tmdbClient.fetchMovies(3)).willReturn(page1);
    given(tmdbClient.fetchMovies(4)).willReturn(page2);

    TmdbMovieReader reader = new TmdbMovieReader(tmdbClient, ContentType.movie, 3, 4, "partition1");

    TmdbEventResponse result1 = reader.read();
    TmdbEventResponse result2 = reader.read();
    TmdbEventResponse result3 = reader.read();

    assertThat(result1.title()).isEqualTo("영화1");
    assertThat(result2.title()).isEqualTo("영화2");
    assertThat(result3).isNull();
  }
}