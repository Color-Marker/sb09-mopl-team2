package com.sb09.sb09moplteam2.batch.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbClient;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbPagePartitioner;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbPageResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class TmdbPagePartitionerTest {

  @Mock
  private TmdbClient tmdbClient;

  @Test
  @DisplayName("totalPages가 maxPages보다 크면 maxPages 기준으로 분할한다")
  void partition_maxPages_기준으로_분할한다() {
    TmdbPageResponse<TmdbEventResponse> response = new TmdbPageResponse<>(List.of(), 100, 100);
    given(tmdbClient.fetchMovies(anyInt(), any())).willReturn(response);

    TmdbPagePartitioner partitioner = new TmdbPagePartitioner(tmdbClient, ContentType.movie, 5);

    Map<String, ExecutionContext> result = partitioner.partition(4);

    assertThat(result).hasSize(3); // 5페이지를 4개로 나누면 ceil(5/4)=2씩, 1~2/3~4/5 총 3개
    ExecutionContext first = result.get("partition0");
    assertThat(first.getInt("startPage")).isEqualTo(1);
    assertThat(first.getInt("endPage")).isEqualTo(2);
    assertThat(first.getString("partitionName")).isEqualTo("partition0");
  }

  @Test
  @DisplayName("totalPages가 maxPages보다 작으면 totalPages 기준으로 분할한다")
  void partition_totalPages가_작으면_totalPages_기준으로_분할한다() {
    TmdbPageResponse<TmdbEventResponse> response = new TmdbPageResponse<>(List.of(), 2, 2);
    given(tmdbClient.fetchMovies(anyInt(), any())).willReturn(response);

    TmdbPagePartitioner partitioner = new TmdbPagePartitioner(tmdbClient, ContentType.movie, 5);

    Map<String, ExecutionContext> result = partitioner.partition(4);

    assertThat(result).hasSize(2); // 전체 2페이지라 파티션도 2개까지만 생김
  }
}