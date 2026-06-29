package com.sb09.sb09moplteam2.content.batch.Sports;

import com.sb09.sb09moplteam2.content.batch.Sports.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class SportsProcessor implements ItemProcessor<SportsEventResponse, Content> {

  private final ContentRepository contentRepository;

  @Override
  public Content process(SportsEventResponse item) {
    if (contentRepository.findByTypeAndExternalId(
        ContentType.sports, item.idEvent()).isPresent()) {
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

    return Content.builder()
        .type(ContentType.sports)
        .externalId(item.idEvent())
        .title(item.strEvent())
        .description(item.description())
        .thumbnailUrl(item.thumbnail())
        .releaseDate(releaseDate)
        .status(status)
        .build();
  }
}