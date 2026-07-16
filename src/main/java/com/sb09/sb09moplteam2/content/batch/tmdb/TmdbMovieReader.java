package com.sb09.sb09moplteam2.content.batch.tmdb;

import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbPageResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class TmdbMovieReader implements ItemReader<TmdbEventResponse> {

  private final TmdbClient tmdbClient;
  private final ContentType contentType;
  private int currentPage = 1;
  private int totalPages = 1;
  private List<TmdbEventResponse> buffer = new ArrayList<>();
  private int bufferIndex = 0;

  public TmdbMovieReader(TmdbClient tmdbClient, ContentType contentType) {
    this.tmdbClient = tmdbClient;
    this.contentType = contentType;
  }

  @Override
  public TmdbEventResponse read() {
    if (bufferIndex >= buffer.size()) {
      if (currentPage > totalPages) {
        return null;
      }
      fetchNextPage();
    }
    if (buffer.isEmpty()) return null;
    return buffer.get(bufferIndex++);
  }

  private void fetchNextPage() {
    TmdbPageResponse<TmdbEventResponse> response = contentType == ContentType.movie
        ? tmdbClient.fetchMovies(currentPage)
        : tmdbClient.fetchTvSeries(currentPage);

    totalPages = Math.min(response.totalPages(), 5); // 가져 올 수 있는 외부 api의 페이지 양
    buffer = response.results();
    bufferIndex = 0;
    currentPage++;
    log.info("TMDB {} 데이터 {}페이지 로드 완료 - {}건", contentType, currentPage - 1, buffer.size());
  }
}