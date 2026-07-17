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
  private int currentPage;
  private final int endPage;
  private final String partitionName;
  private List<TmdbEventResponse> buffer = new ArrayList<>();
  private int bufferIndex = 0;

  public TmdbMovieReader(TmdbClient tmdbClient, ContentType contentType, int startPage, int endPage, String partitionName) {
    this.tmdbClient = tmdbClient;
    this.contentType = contentType;
    this.currentPage = startPage;
    this.endPage = endPage;
    this.partitionName = partitionName;
  }

  @Override
  public TmdbEventResponse read() {
    if (bufferIndex >= buffer.size()) {
      if (currentPage > endPage) {
        return null;
      }
      fetchNextPage();
    }
    if (buffer.isEmpty()) return null;
    return buffer.get(bufferIndex++);
  }

  private void fetchNextPage() {
    try {
      TmdbPageResponse<TmdbEventResponse> response = contentType == ContentType.movie
          ? tmdbClient.fetchMovies(currentPage)
          : tmdbClient.fetchTvSeries(currentPage);

      buffer = response.results();
      bufferIndex = 0;
      log.info("TMDB {} 데이터 {}페이지 로드 완료 - {}건 ({})",
          contentType, currentPage, buffer.size(), partitionName);
    } catch (Exception e) {
      log.warn("TMDB {} {}페이지 조회 실패 - 스킵하고 다음 페이지로 진행 ({}): {}",
          contentType, currentPage, partitionName, e.getMessage());
      buffer = List.of();
      bufferIndex = 0;
    }
    currentPage++;
  }
}