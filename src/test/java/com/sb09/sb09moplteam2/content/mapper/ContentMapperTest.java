package com.sb09.sb09moplteam2.content.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentMapperTest {

  private final ContentMapper contentMapper = new ContentMapper();

  @Mock
  private Content content;

  @Test
  @DisplayName("toDto - Content와 태그 목록을 ContentDto로 변환한다")
  void toDto_Content와_태그_목록을_ContentDto로_변환한다() {
    UUID contentId = UUID.randomUUID();
    given(content.getId()).willReturn(contentId);
    given(content.getType()).willReturn(ContentType.movie);
    given(content.getTitle()).willReturn("테스트 영화");
    given(content.getDescription()).willReturn("설명");
    given(content.getThumbnailUrl()).willReturn("/files/poster.jpg");
    given(content.getAverageRating()).willReturn(4.5);
    given(content.getReviewCount()).willReturn(10);
    given(content.getWatcherCount()).willReturn(100L);

    ContentTag tag1 = mock(ContentTag.class);
    ContentTag tag2 = mock(ContentTag.class);
    given(tag1.getTag()).willReturn("액션");
    given(tag2.getTag()).willReturn("SF");

    ContentDto result = contentMapper.toDto(content, List.of(tag1, tag2));

    assertThat(result.id()).isEqualTo(contentId);
    assertThat(result.type()).isEqualTo(ContentType.movie);
    assertThat(result.title()).isEqualTo("테스트 영화");
    assertThat(result.description()).isEqualTo("설명");
    assertThat(result.thumbnailUrl()).isEqualTo("/files/poster.jpg");
    assertThat(result.tags()).containsExactly("액션", "SF");
    assertThat(result.averageRating()).isEqualTo(4.5);
    assertThat(result.reviewCount()).isEqualTo(10);
    assertThat(result.watcherCount()).isEqualTo(100L);
  }

  @Test
  @DisplayName("toDto - 태그가 없으면 빈 태그 목록을 반환한다")
  void toDto_태그가_없으면_빈_태그_목록을_반환한다() {
    given(content.getId()).willReturn(UUID.randomUUID());
    given(content.getType()).willReturn(ContentType.tvSeries);
    given(content.getTitle()).willReturn("테스트 드라마");
    given(content.getDescription()).willReturn("설명");
    given(content.getThumbnailUrl()).willReturn(null);
    given(content.getAverageRating()).willReturn(0.0);
    given(content.getReviewCount()).willReturn(0);
    given(content.getWatcherCount()).willReturn(0L);

    ContentDto result = contentMapper.toDto(content, List.of());

    assertThat(result.tags()).isEmpty();
    assertThat(result.thumbnailUrl()).isNull();
  }

  @Test
  @DisplayName("toContentSummary - Content와 태그 목록을 ContentSummary로 변환한다")
  void toContentSummary_Content와_태그_목록을_ContentSummary로_변환한다() {
    UUID contentId = UUID.randomUUID();
    given(content.getId()).willReturn(contentId);
    given(content.getType()).willReturn(ContentType.sport);
    given(content.getTitle()).willReturn("결승전");
    given(content.getDescription()).willReturn("경기 설명");
    given(content.getThumbnailUrl()).willReturn("/files/match.jpg");
    given(content.getAverageRating()).willReturn(3.8);
    given(content.getReviewCount()).willReturn(5);

    ContentTag tag = mock(ContentTag.class);
    given(tag.getTag()).willReturn("축구");

    ContentSummary result = contentMapper.toContentSummary(content, List.of(tag));

    assertThat(result.id()).isEqualTo(contentId);
    assertThat(result.type()).isEqualTo(ContentType.sport);
    assertThat(result.title()).isEqualTo("결승전");
    assertThat(result.description()).isEqualTo("경기 설명");
    assertThat(result.thumbnailUrl()).isEqualTo("/files/match.jpg");
    assertThat(result.tags()).containsExactly("축구");
    assertThat(result.averageRating()).isEqualTo(3.8);
    assertThat(result.reviewCount()).isEqualTo(5);
  }

  @Test
  @DisplayName("toContentSummary - 태그가 없으면 빈 태그 목록을 반환한다")
  void toContentSummary_태그가_없으면_빈_태그_목록을_반환한다() {
    given(content.getId()).willReturn(UUID.randomUUID());
    given(content.getType()).willReturn(ContentType.movie);
    given(content.getTitle()).willReturn("제목");
    given(content.getDescription()).willReturn("설명");
    given(content.getThumbnailUrl()).willReturn(null);
    given(content.getAverageRating()).willReturn(0.0);
    given(content.getReviewCount()).willReturn(0);

    ContentSummary result = contentMapper.toContentSummary(content, List.of());

    assertThat(result.tags()).isEmpty();
  }

  private static ContentTag mock(Class<ContentTag> clazz) {
    return org.mockito.Mockito.mock(clazz);
  }
}