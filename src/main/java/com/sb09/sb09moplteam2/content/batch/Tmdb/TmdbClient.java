package com.sb09.sb09moplteam2.content.batch.Tmdb;

import com.sb09.sb09moplteam2.content.batch.Tmdb.dto.TmdbMovieResponse;
import com.sb09.sb09moplteam2.content.batch.Tmdb.dto.TmdbPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbClient {

  private final TmdbProperties tmdbProperties;
  private final RestClient restClient;

  public TmdbPageResponse<TmdbMovieResponse> fetchMovies(int page) {
    log.info("TMDB 영화 데이터 호출 - page: {}", page);
    return restClient.get()
        .uri(tmdbProperties.baseUrl() + "/3/movie/popular?api_key={key}&page={page}&language=ko-KR",
            tmdbProperties.key(), page)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  public TmdbPageResponse<TmdbMovieResponse> fetchDramas(int page) {
    log.info("TMDB 드라마 데이터 호출 - page: {}", page);
    return restClient.get()
        .uri(tmdbProperties.baseUrl() + "/3/tv/popular?api_key={key}&page={page}&language=ko-KR",
            tmdbProperties.key(), page)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}