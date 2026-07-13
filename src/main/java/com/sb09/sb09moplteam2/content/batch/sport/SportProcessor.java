package com.sb09.sb09moplteam2.content.batch.sport;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class SportProcessor implements ItemProcessor<SportsEventResponse, ContentAndTags> {

  private final ContentRepository contentRepository;

  @Override
  public ContentAndTags process(SportsEventResponse item) {
    if (contentRepository.findByTypeAndExternalId(
        ContentType.sport, item.idEvent()).isPresent()) {
      log.info("이미 존재하는 스포츠 콘텐츠 skip - externalId: {}", item.idEvent());
      return null;
    }

    LocalDate releaseDate = null;
    if (item.dateEvent() != null && !item.dateEvent().isBlank()) {
      try {
        releaseDate = LocalDate.parse(item.dateEvent());
      } catch (Exception e) {
        log.warn("날짜 파싱 실패: {}", item.dateEvent());
      }
    }

    // 날짜 기준으로 UPCOMING/RELEASE 구분
    String status = (releaseDate != null && releaseDate.isAfter(LocalDate.now()))
        ? "UPCOMING" : "RELEASE";

    Content content = Content.builder()
        .type(ContentType.sport)
        .externalId(item.idEvent())
        .title(item.strEvent())
        .description(item.description())
        .thumbnailUrl(item.thumbnail())
        .releaseDate(releaseDate)
        .status(status)
        .build();

    List<String> tags = (item.league() != null && !item.league().isBlank())
        ? List.of(item.league())
        : List.of();

    return new ContentAndTags(content, tags);
  }
}