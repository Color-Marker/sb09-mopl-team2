package com.sb09.sb09moplteam2.batch.sport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.batch.sport.SportProcessor;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.time.LocalDate;
import java.util.Optional;
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
    Content existing = Content.builder()
        .type(ContentType.sport)
        .externalId("1001")
        .title("결승전")
        .build();
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "1001"))
        .willReturn(Optional.of(existing));

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result).isNull();
  }

  @Test
  void 존재하지_않는_콘텐츠는_Content로_변환된다() {
    SportsEventResponse item = new SportsEventResponse(
        "2001", "결승전", "설명입니다", "thumb.jpg", "2026-08-01", "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "2001"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(ContentType.sport);
    assertThat(result.getExternalId()).isEqualTo("2001");
    assertThat(result.getTitle()).isEqualTo("결승전");
    assertThat(result.getDescription()).isEqualTo("설명입니다");
    assertThat(result.getThumbnailUrl()).isEqualTo("thumb.jpg");
  }

  @Test
  void 날짜가_미래인_경우_UPCOMING_상태로_설정된다() {
    String futureDate = LocalDate.now().plusDays(10).toString();
    SportsEventResponse item = new SportsEventResponse(
        "3001", "미래경기", "설명", "thumb.jpg", futureDate, "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "3001"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result.getStatus()).isEqualTo("UPCOMING");
    assertThat(result.getReleaseDate()).isEqualTo(LocalDate.parse(futureDate));
  }

  @Test
  void 날짜가_과거인_경우_RELEASE_상태로_설정된다() {
    String pastDate = LocalDate.now().minusDays(10).toString();
    SportsEventResponse item = new SportsEventResponse(
        "4001", "과거경기", "설명", "thumb.jpg", pastDate, "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "4001"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result.getStatus()).isEqualTo("RELEASE");
    assertThat(result.getReleaseDate()).isEqualTo(LocalDate.parse(pastDate));
  }

  @Test
  void 날짜가_오늘인_경우_RELEASE_상태로_설정된다() {
    String today = LocalDate.now().toString();
    SportsEventResponse item = new SportsEventResponse(
        "4002", "오늘경기", "설명", "thumb.jpg", today, "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "4002"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result.getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void 날짜가_null인_경우_releaseDate는_null이고_RELEASE_상태이다() {
    SportsEventResponse item = new SportsEventResponse(
        "5001", "경기", "설명", "thumb.jpg", null, "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "5001"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result.getReleaseDate()).isNull();
    assertThat(result.getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void 날짜가_빈문자열인_경우_releaseDate는_null이고_RELEASE_상태이다() {
    SportsEventResponse item = new SportsEventResponse(
        "5002", "경기", "설명", "thumb.jpg", "  ", "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "5002"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result.getReleaseDate()).isNull();
    assertThat(result.getStatus()).isEqualTo("RELEASE");
  }

  @Test
  void 날짜_형식이_잘못된_경우_releaseDate는_null이고_RELEASE_상태이다() {
    SportsEventResponse item = new SportsEventResponse(
        "5003", "경기", "설명", "thumb.jpg", "잘못된날짜", "축구");
    given(contentRepository.findByTypeAndExternalId(ContentType.sport, "5003"))
        .willReturn(Optional.empty());

    SportProcessor processor = new SportProcessor(contentRepository);

    Content result = processor.process(item);

    assertThat(result.getReleaseDate()).isNull();
    assertThat(result.getStatus()).isEqualTo("RELEASE");
  }
}