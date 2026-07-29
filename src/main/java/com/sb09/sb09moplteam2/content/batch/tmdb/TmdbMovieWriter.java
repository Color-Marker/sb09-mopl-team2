package com.sb09.sb09moplteam2.content.batch.tmdb;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import com.sb09.sb09moplteam2.content.search.ContentDocument;
import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class TmdbMovieWriter implements ItemWriter<ContentAndTags> {

  private final ContentRepository contentRepository;
  private final ContentTagRepository contentTagRepository;
  private final ContentSearchService contentSearchService;

  @Override
  public void write(Chunk<? extends ContentAndTags> chunk) {
    List<Content> contents = chunk.getItems().stream()
        .map(ContentAndTags::content)
        .toList();
    contentRepository.saveAll(contents);

    List<ContentTag> tags = new ArrayList<>();
    for (ContentAndTags item : chunk.getItems()) {
      for (String tag : item.tags()) {
        tags.add(ContentTag.builder()
            .content(item.content())
            .tag(tag)
            .build());
      }
    }
    if (!tags.isEmpty()) {
      contentTagRepository.saveAll(tags);
    }

    // 콘텐츠별 태그를 묶어서 검색엔진에 색인
    Map<java.util.UUID, List<String>> tagMap = chunk.getItems().stream()
        .collect(Collectors.toMap(
            item -> item.content().getId(),
            ContentAndTags::tags
        ));

    List<ContentDocument> documents = contents.stream()
        .map(content -> ContentDocument.from(
            content, tagMap.getOrDefault(content.getId(), List.of())))
        .toList();

    try {
      contentSearchService.indexAll(documents);
    } catch (Exception e) {
      log.warn("배치 색인 실패 - {}건, error: {}", documents.size(), e.getMessage());
    }

    log.info("콘텐츠 저장 완료 - {}건 (태그 {}건 포함)", contents.size(), tags.size());
  }
}