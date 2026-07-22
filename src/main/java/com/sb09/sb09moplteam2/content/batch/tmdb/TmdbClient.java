package com.sb09.sb09moplteam2.content.batch.tmdb;

import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbClient {

  private final TmdbProperties tmdbProperties;
  private final RestClient restClient;

  @Retryable(
      retryFor = { HttpServerErrorException.class, ResourceAccessException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 15000)
  )
  public TmdbPageResponse<TmdbEventResponse> fetchMovies(int page, String sortBy) {
    log.info("TMDB 영화 데이터 호출 - page: {}, sortBy: {}", page, sortBy);
    return restClient.get()
        .uri(tmdbProperties.baseUrl()
                + "/3/discover/movie?api_key={key}&page={page}&language=ko-KR&sort_by={sortBy}&vote_count.gte=100",
            tmdbProperties.key(), page, sortBy)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  @Retryable(
      retryFor = { HttpServerErrorException.class, ResourceAccessException.class },
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public TmdbPageResponse<TmdbEventResponse> fetchTvSeries(int page, String sortBy) {
    log.info("TMDB TV시리즈 데이터 호출 - page: {}, sortBy: {}", page, sortBy);
    return restClient.get()
        .uri(tmdbProperties.baseUrl()
                + "/3/discover/tv?api_key={key}&page={page}&language=ko-KR&sort_by={sortBy}&vote_count.gte=100",
            tmdbProperties.key(), page, sortBy)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}