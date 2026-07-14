package com.sb09.sb09moplteam2.batch.sport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.sport.SportProcessor;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SportProcessorTest {

  @Mock
  private ContentRepository contentRepository;

  @Test
  void 이미_존재하는_콘텐츠는_스킵하고_null을_반환한다() {
    SportsEventResponse item = new SportsEventResponse(
        "1001", "결승전", "설명", "thumb.jpg", "2026-08-01", "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of("1001"));

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result).isNull();
  }

  @Test
  void 존재하지_않는_콘텐츠는_Content로_변환된다() {
    SportsEventResponse item = new SportsEventResponse(
        "2001", "결승전", "설명입니다", "thumb.jpg", "2026-08-01", "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result).isNotNull();
    assertThat(result.content().getType()).isEqualTo(ContentType.sport);
    assertThat(result.content().getExternalId()).isEqualTo("2001");
    assertThat(result.content().getTitle()).isEqualTo("결승전");
    assertThat(result.content().getDescription()).isEqualTo("설명입니다");
    assertThat(result.content().getThumbnailUrl()).isEqualTo("thumb.jpg");
  }

  @Test
  void 날짜가_미래인_경우_UPCOMING_상태로_설정된다() {
    String futureDate = LocalDate.now().plusDays(10).toString();
    SportsEventResponse item = new SportsEventResponse(
        "3001", "미래경기", "설명", "thumb.jpg", futureDate, "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getStatus()).isEqualTo("UPCOMING");
    assertThat(result.content().getReleaseDate()).isEqualTo(LocalDate.parse(futureDate));
  }

  @Test
  void 날짜가_과거인_경우_RELEASE_상태로_설정된다() {
    String pastDate = LocalDate.now().minusDays(10).toString();
    SportsEventResponse item = new SportsEventResponse(
        "4001", "과거경기", "설명", "thumb.jpg", pastDate, "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getStatus()).isEqualTo("RELEASE");
    assertThat(result.content().getReleaseDate()).isEqualTo(LocalDate.parse(pastDate));
  }

  @Test
  void 날짜가_오늘인_경우_RELEASE_상태로_설정된다() {
    String today = LocalDate.now().toString();
    SportsEventResponse item = new SportsEventResponse(
        "4002", "오늘경기", "설명", "thumb.jpg", today, "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void 날짜가_null인_경우_releaseDate는_null이고_RELEASE_상태이다() {
    SportsEventResponse item = new SportsEventResponse(
        "5001", "경기", "설명", "thumb.jpg", null, "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getReleaseDate()).isNull();
    assertThat(result.content().getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void 날짜가_빈문자열인_경우_releaseDate는_null이고_RELEASE_상태이다() {
    SportsEventResponse item = new SportsEventResponse(
        "5002", "경기", "설명", "thumb.jpg", "  ", "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getReleaseDate()).isNull();
    assertThat(result.content().getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void 날짜_형식이_잘못된_경우_releaseDate는_null이고_RELEASE_상태이다() {
    SportsEventResponse item = new SportsEventResponse(
        "5003", "경기", "설명", "thumb.jpg", "잘못된날짜", "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.content().getReleaseDate()).isNull();
    assertThat(result.content().getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void league가_있으면_태그로_변환된다() {
    SportsEventResponse item = new SportsEventResponse(
        "6001", "경기", "설명", "thumb.jpg", "2026-08-01", "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.tags()).containsExactly("축구");
  }

  @Test
  void league가_없으면_빈_태그_목록을_반환한다() {
    SportsEventResponse item = new SportsEventResponse(
        "6002", "경기", "설명", "thumb.jpg", "2026-08-01", null);
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags result = processor.process(item);

    assertThat(result.tags()).isEmpty();
  }

  @Test
  void 이번_실행_중_같은_externalId가_다시_나오면_두번째부터_스킵된다() {
    SportsEventResponse item = new SportsEventResponse(
        "7001", "경기", "설명", "thumb.jpg", "2026-08-01", "축구");
    given(contentRepository.findAllExternalIdsByType(ContentType.sport))
        .willReturn(Set.of());

    SportProcessor processor = new SportProcessor(contentRepository);

    ContentAndTags first = processor.process(item);
    ContentAndTags second = processor.process(item);

    assertThat(first).isNotNull();
    assertThat(second).isNull();
  }
}