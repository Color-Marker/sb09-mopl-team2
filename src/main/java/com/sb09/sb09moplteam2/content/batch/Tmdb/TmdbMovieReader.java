package com.sb09.sb09moplteam2.content.batch.Tmdb;

import com.sb09.sb09moplteam2.content.batch.Tmdb.dto.TmdbMovieResponse;
import com.sb09.sb09moplteam2.content.batch.Tmdb.dto.TmdbPageResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class TmdbMovieReader implements ItemReader<TmdbMovieResponse> {

  private final TmdbClient tmdbClient;
  private final ContentType contentType;
  private int currentPage = 1;
  private int totalPages = 1;
  private List<TmdbMovieResponse> buffer = new ArrayList<>();
  private int bufferIndex = 0;

  public TmdbMovieReader(TmdbClient tmdbClient, ContentType contentType) {
    this.tmdbClient = tmdbClient;
    this.contentType = contentType;
  }

  @Override
  public TmdbMovieResponse read() {
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
    TmdbPageResponse<TmdbMovieResponse> response = contentType == ContentType.movie
        ? tmdbClient.fetchMovies(currentPage)
        : tmdbClient.fetchDramas(currentPage);

    totalPages = Math.min(response.totalPages(), 5); // 최대 5페이지
    buffer = response.results();
    bufferIndex = 0;
    currentPage++;
    log.info("TMDB {} 데이터 {}페이지 로드 완료 - {}건", contentType, currentPage - 1, buffer.size());
  }
}