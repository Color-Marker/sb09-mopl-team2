package com.sb09.sb09moplteam2.content.batch.tmdb;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.tmdb.dto.TmdbEventResponse;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class TmdbMovieProcessor implements ItemProcessor<TmdbEventResponse, ContentAndTags> {

  private final ContentRepository contentRepository;
  private final ContentType contentType;
  private final Set<String> existingExternalIds;
  private final Set<String> processedInThisRun = new HashSet<>();


  public TmdbMovieProcessor(ContentRepository contentRepository, ContentType contentType) {
    this.contentRepository = contentRepository;
    this.contentType = contentType;
    this.existingExternalIds = new HashSet<>(
        contentRepository.findAllExternalIdsByType(contentType));
    log.info("기존 {} 콘텐츠 {}건 로드 완료", contentType, existingExternalIds.size());
  }


  @Override
  public ContentAndTags process(TmdbEventResponse item) {
    String externalId = String.valueOf(item.id());

    if (!existingExternalIds.add(externalId)) {
      log.info("이미 처리된 콘텐츠 skip - externalId: {}", externalId);
      return null;
    }

    Content content = Content.builder()
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

    List<String> tags = TmdbGenreMapper.toTagNames(item.genreIds());

    return new ContentAndTags(content, tags);
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