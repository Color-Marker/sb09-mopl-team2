package com.sb09.sb09moplteam2.batch.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieProcessor;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmdbMovieProcessorTest {

  @Mock
  private ContentRepository contentRepository;

  @Test
  @DisplayName("이미 존재하는 콘텐츠는 null을 반환한다")
  void process_이미_존재하는_콘텐츠는_null을_반환한다() {
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(
        1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01", List.of(28));

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.of(mock(Content.class)));

    ContentAndTags result = processor.process(item);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("새로운 movie 콘텐츠를 생성한다")
  void process_새로운_movie_콘텐츠를_생성한다() {
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(
        1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01", List.of(28));

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.empty());

    ContentAndTags result = processor.process(item);

    assertThat(result).isNotNull();
    assertThat(result.content().getTitle()).isEqualTo("테스트 영화");
    assertThat(result.content().getType()).isEqualTo(ContentType.movie);
  }

  @Test
  @DisplayName("새로운 tvSeries 콘텐츠를 생성한다")
  void process_새로운_tvSeries_콘텐츠를_생성한다() {
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.tvSeries);
    TmdbEventResponse item = new TmdbEventResponse(
        2L, null, "테스트 드라마", "줄거리", "/poster.jpg", null, List.of(18));

    given(contentRepository.findByTypeAndExternalId(ContentType.tvSeries, "2"))
        .willReturn(Optional.empty());

    ContentAndTags result = processor.process(item);

    assertThat(result).isNotNull();
    assertThat(result.content().getTitle()).isEqualTo("테스트 드라마");
    assertThat(result.content().getType()).isEqualTo(ContentType.tvSeries);
  }

  @Test
  @DisplayName("releaseDate가 null이면 null을 반환한다")
  void process_releaseDate가_null이면_null을_반환한다() {
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(
        1L, "테스트 영화", null, "줄거리", null, null, List.of());

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.empty());

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getReleaseDate()).isNull();
    assertThat(result.content().getThumbnailUrl()).isNull();
  }

  @Test
  @DisplayName("genreIds가 있으면 태그로 변환된다")
  void process_genreIds가_있으면_태그로_변환된다() {
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(
        1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01", List.of(28, 878));

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.empty());

    ContentAndTags result = processor.process(item);

    assertThat(result.tags()).containsExactlyInAnyOrder("액션", "SF");
  }

  @Test
  @DisplayName("genreIds가 없으면 빈 태그 목록을 반환한다")
  void process_genreIds가_없으면_빈_태그_목록을_반환한다() {
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(
        1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01", null);

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.empty());

    ContentAndTags result = processor.process(item);

    assertThat(result.tags()).isEmpty();
  }
}