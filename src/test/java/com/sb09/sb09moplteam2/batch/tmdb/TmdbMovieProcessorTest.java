package com.sb09.sb09moplteam2.batch.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieProcessor;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
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
    // given
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01");

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.of(mock(Content.class)));

    // when
    Content result = processor.process(item);

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("새로운 movie 콘텐츠를 생성한다")
  void process_새로운_movie_콘텐츠를_생성한다() {
    // given
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01");

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.empty());

    // when
    Content result = processor.process(item);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("테스트 영화");
    assertThat(result.getType()).isEqualTo(ContentType.movie);
  }

  @Test
  @DisplayName("새로운 tvSeries 콘텐츠를 생성한다")
  void process_새로운_tvSeries_콘텐츠를_생성한다() {
    // given
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.tvSeries);
    TmdbEventResponse item = new TmdbEventResponse(2L, null, "테스트 드라마", "줄거리", "/poster.jpg", null);

    given(contentRepository.findByTypeAndExternalId(ContentType.tvSeries, "2"))
        .willReturn(Optional.empty());

    // when
    Content result = processor.process(item);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("테스트 드라마");
    assertThat(result.getType()).isEqualTo(ContentType.tvSeries);
  }

  @Test
  @DisplayName("releaseDate가 null이면 null을 반환한다")
  void process_releaseDate가_null이면_null을_반환한다() {
    // given
    TmdbMovieProcessor processor = new TmdbMovieProcessor(contentRepository, ContentType.movie);
    TmdbEventResponse item = new TmdbEventResponse(1L, "테스트 영화", null, "줄거리", null, null);

    given(contentRepository.findByTypeAndExternalId(ContentType.movie, "1"))
        .willReturn(Optional.empty());

    // when
    Content result = processor.process(item);

    // then
    assertThat(result.getReleaseDate()).isNull();
    assertThat(result.getThumbnailUrl()).isNull();
  }
}

