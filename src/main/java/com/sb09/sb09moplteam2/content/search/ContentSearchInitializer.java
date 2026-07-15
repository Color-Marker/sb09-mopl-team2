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

  @EventListener(ApplicationReadyEvent.class)
  public void reindexAll() {
    List<Content> allContents = contentRepository.findAll();
    allContents.forEach(content ->
        contentSearchService.index(ContentDocument.from(content)));
    log.info("Elasticsearch 초기 색인 완료 - {}건", allContents.size());
  }
}