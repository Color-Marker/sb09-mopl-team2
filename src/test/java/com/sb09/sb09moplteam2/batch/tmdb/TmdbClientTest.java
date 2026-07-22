package com.sb09.sb09moplteam2.batch.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbClient;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbProperties;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbPageResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class TmdbClientTest {

  @Mock
  private TmdbProperties tmdbProperties;

  @Mock
  private RestClient restClient;

  @Mock
  private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private RestClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private RestClient.ResponseSpec responseSpec;

  private TmdbClient tmdbClient;

  @BeforeEach
  void setUp() {
    tmdbClient = new TmdbClient(tmdbProperties, restClient);
  }

  @Test
  @DisplayName("영화 목록을 요청하고 응답을 반환한다")
  void fetchMovies_영화_목록을_요청하고_응답을_반환한다() {
    given(tmdbProperties.baseUrl()).willReturn("https://api.themoviedb.org");
    given(tmdbProperties.key()).willReturn("test-api-key");

    TmdbEventResponse item = new TmdbEventResponse(
        1L, "테스트 영화", null, "줄거리", "/poster.jpg", "2024-01-01", List.of(28));
    TmdbPageResponse<TmdbEventResponse> expected = new TmdbPageResponse<>(List.of(item), 1, 1);

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
        .willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(any(ParameterizedTypeReference.class))).willReturn(expected);

    TmdbPageResponse<TmdbEventResponse> result = tmdbClient.fetchMovies(1, "popularity.desc");

    assertThat(result).isEqualTo(expected);
    assertThat(result.results()).hasSize(1);
    assertThat(result.results().get(0).title()).isEqualTo("테스트 영화");
  }

  @Test
  @DisplayName("영화 목록 요청 시 올바른 URI 템플릿과 파라미터를 사용한다")
  void fetchMovies_올바른_URI와_파라미터를_사용한다() {
    given(tmdbProperties.baseUrl()).willReturn("https://api.themoviedb.org");
    given(tmdbProperties.key()).willReturn("test-api-key");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
        .willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(any(ParameterizedTypeReference.class)))
        .willReturn(new TmdbPageResponse<>(List.of(), 1, 1));

    ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object[]> varsCaptor = ArgumentCaptor.forClass(Object[].class);

    tmdbClient.fetchMovies(3, "vote_average.desc");

    verify(requestHeadersUriSpec).uri(uriCaptor.capture(), varsCaptor.capture());
    assertThat(uriCaptor.getValue()).contains("/3/discover/movie");
    assertThat(varsCaptor.getValue()).containsExactly("test-api-key", 3, "vote_average.desc");
  }

  @Test
  @DisplayName("TV 시리즈 목록을 요청하고 응답을 반환한다")
  void fetchTvSeries_TV_시리즈_목록을_요청하고_응답을_반환한다() {
    given(tmdbProperties.baseUrl()).willReturn("https://api.themoviedb.org");
    given(tmdbProperties.key()).willReturn("test-api-key");

    TmdbEventResponse item = new TmdbEventResponse(
        2L, null, "테스트 드라마", "줄거리", "/poster.jpg", null, List.of(18));
    TmdbPageResponse<TmdbEventResponse> expected = new TmdbPageResponse<>(List.of(item), 1, 1);

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
        .willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(any(ParameterizedTypeReference.class))).willReturn(expected);

    TmdbPageResponse<TmdbEventResponse> result = tmdbClient.fetchTvSeries(1, "popularity.desc");

    assertThat(result).isEqualTo(expected);
    assertThat(result.results().get(0).name()).isEqualTo("테스트 드라마");
  }

  @Test
  @DisplayName("TV 시리즈 요청 시 올바른 URI 템플릿을 사용한다")
  void fetchTvSeries_올바른_URI를_사용한다() {
    given(tmdbProperties.baseUrl()).willReturn("https://api.themoviedb.org");
    given(tmdbProperties.key()).willReturn("test-api-key");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
        .willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(any(ParameterizedTypeReference.class)))
        .willReturn(new TmdbPageResponse<>(List.of(), 1, 1));

    ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);

    tmdbClient.fetchTvSeries(2, "first_air_date.desc");

    verify(requestHeadersUriSpec).uri(uriCaptor.capture(), any(Object[].class));
    assertThat(uriCaptor.getValue()).contains("/3/discover/tv");
  }
}