package com.sb09.sb09moplteam2.content.batch.tmdb;


import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbPageResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
@RequiredArgsConstructor
public class TmdbPagePartitioner implements Partitioner {

  private final TmdbClient tmdbClient;
  private final ContentType contentType;
  private final int maxPages;

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {

    int totalPages = fetchTotalPages();
    Map<String, ExecutionContext> partitions = new HashMap<>();

    int pagesPerPartition = (int) Math.ceil((double) totalPages / gridSize);
    int start = 1;
    int partitionNumber = 0;

    while(start <= totalPages) {
      int end =  Math.min(start + pagesPerPartition - 1, totalPages);

      ExecutionContext context = new ExecutionContext();
      context.putInt("startPage", start);
      context.putInt("endPage", end);
      context.putString("partitionName", "partition" + partitionNumber);
      partitions.put("partition" + partitionNumber, context);

      log.info("{} 파티션 {} 생성 = {}~{}페이지", contentType, partitionNumber, start, end);

      start = end + 1;
      partitionNumber++;
    }

    return partitions;
  }


  private int fetchTotalPages() {
    TmdbPageResponse<TmdbEventResponse> response = contentType == ContentType.movie
        ? tmdbClient.fetchMovies(1)
        : tmdbClient.fetchTvSeries(1);

    return Math.min(response.totalPages(), maxPages);
  }
}
