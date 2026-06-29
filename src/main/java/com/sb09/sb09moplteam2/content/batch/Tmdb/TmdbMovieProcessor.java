package com.sb09.sb09moplteam2.content.batch.Tmdb;

import com.sb09.sb09moplteam2.content.batch.Tmdb.dto.TmdbMovieResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
public class TmdbMovieProcessor implements ItemProcessor<TmdbMovieResponse, Content> {

  private final ContentRepository contentRepository;
  private final ContentType contentType;


  @Override
  public Content process(TmdbMovieResponse item) {
    if (contentRepository.findByTypeAndExternalId(
        contentType, String.valueOf(item.id())).isPresent()) {
      log.info("이미 존재하는 콘텐츠 skip - externalId: {}", item.id());
      return null;
    }

    return Content.builder()
        .type(contentType)
        .externalId(String.valueOf(item.id()))
        .title(contentType == ContentType.movie ? item.title() : item.name())
        .description(item.overview())
        .thumbnailUrl(item.posterPath() != null
            ? "https://image.tmdb.org/t/p/w500" + item.posterPath()
            : null)
        .releaseDate(parseDate(item.releaseDate()))
        .status("RELEASE")
        .build();
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) return null;
    try {
      return LocalDate.parse(date);
    } catch (Exception e) {
      return null;
    }
  }
}