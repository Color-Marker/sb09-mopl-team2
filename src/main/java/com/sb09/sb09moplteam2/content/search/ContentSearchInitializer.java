package com.sb09.sb09moplteam2.content.search;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSearchInitializer {

  private final ContentRepository contentRepository;
  private final ContentSearchService contentSearchService;
  private final ContentSearchRepository contentSearchRepository;

  @EventListener(ApplicationReadyEvent.class)
  public void reindexAll() {
    long existingCount = contentSearchRepository.count();
    if (existingCount > 0) {
      log.info("Elasticsearch에 이미 색인된 데이터가 있어 재색인을 건너뜁니다 - {}건", existingCount);
      return;
    }

    List<Content> allContents = contentRepository.findAll();
    allContents.forEach(content ->
        contentSearchService.index(ContentDocument.from(content)));
    log.info("Elasticsearch 색인 완료 - {}건", allContents.size());
  }
}