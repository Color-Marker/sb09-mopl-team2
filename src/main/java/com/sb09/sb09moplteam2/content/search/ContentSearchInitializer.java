package com.sb09.sb09moplteam2.content.search;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSearchInitializer {

  private final ContentRepository contentRepository;
  private final ContentSearchService contentSearchService;
  private final ContentSearchRepository contentSearchRepository;
  private final ElasticsearchOperations elasticsearchOperations;
  private final ContentTagRepository contentTagRepository;

  @EventListener(ApplicationReadyEvent.class)
  public void reindexAll() {
    try {
      // ContentDocument가 createIndex = false라서 인덱스 생성은 여기서 수행 (ES 미가용이면 catch로 넘어감)
      IndexOperations indexOps = elasticsearchOperations.indexOps(ContentDocument.class);
      if (!indexOps.exists()) {
        indexOps.createWithMapping();
        log.info("Elasticsearch contents 인덱스를 생성했습니다");
      }

      long existingCount = contentSearchRepository.count();
      if (existingCount > 0) {
        log.info("Elasticsearch에 이미 색인된 데이터가 있어 재색인을 건너뜁니다 - {}건", existingCount);
        return;
      }

      List<Content> allContents = contentRepository.findAll();

      Map<UUID, List<String>> tagMap = contentTagRepository.findAll().stream()
          .collect(Collectors.groupingBy(
              tag -> tag.getContent().getId(),
              Collectors.mapping(ContentTag::getTag, Collectors.toList())
          ));
      allContents.forEach(content ->{
            List<String> tags = tagMap.getOrDefault(content.getId(), List.of());
            contentSearchService.index(ContentDocument.from(content, tags));
          });
      log.info("Elasticsearch 색인 완료 - {}건", allContents.size());
    } catch (Exception e) {
      // ES 미가용 시 검색 기능만 비활성화하고 앱은 정상 기동 (연결 복구 후 재기동하면 재색인됨)
      log.warn("Elasticsearch 연결 실패로 콘텐츠 재색인을 건너뜁니다: {}", e.getMessage());
    }
  }
}