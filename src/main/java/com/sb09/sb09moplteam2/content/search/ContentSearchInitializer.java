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

  private static final int CHUNK_SIZE = 20; // 1청크 당 20건식 색인 터진 이력이 있어 보수적으로 지정했습니다 여유가 있으면 계속 늘려가는 방식으로 해도 됩니다
  private static final long DELAY_BETWEEN_CHUNKS_MILLIS = 2000; // 1청크 당 20건식 이후 2초 후 다음 청크 색인

  @EventListener(ApplicationReadyEvent.class)
  public void reindexAll() {
    try {
      // ContentDocument가 createIndex = false라서 인덱스 생성은 여기서 수행 (ES 미가용이면 catch로 넘어감)
      IndexOperations indexOps = elasticsearchOperations.indexOps(ContentDocument.class);
      if (!indexOps.exists()) {
        indexOps.createWithMapping();
        log.info("Elasticsearch/Opensearch contents 인덱스를 생성했습니다");
      }

      long existingCount = contentSearchRepository.count();
      if (existingCount > 0) {
        log.info("Elasticsearch/Opensearch 에 이미 색인된 데이터가 있어 재색인을 건너뜁니다 - {}건", existingCount);
        return;
      }

      List<Content> allContents = contentRepository.findAll();

      Map<UUID, List<String>> tagMap = contentTagRepository.findAll().stream()
          .collect(Collectors.groupingBy(
              tag -> tag.getContent().getId(),
              Collectors.mapping(ContentTag::getTag, Collectors.toList())
          ));

      int total = allContents.size();

      for(int i = 0; i < total; i+= CHUNK_SIZE) {
        int end = Math.min(i + CHUNK_SIZE, total);
        List<Content> chunk = allContents.subList(i, end);

        List<ContentDocument> documents = chunk.stream()
            .map(content -> ContentDocument.from(
                content, tagMap.getOrDefault(content.getId(), List.of())))
            .toList();

        try{
          contentSearchService.indexAll(documents);
          log.info("색인 진행 중 - {}/{} 건 완료", end, total);
        } catch (Exception e){
          log.warn("청크 색인 실패 - {}~{}, error: {}", i, end, e.getMessage());
        }

        if(end < total) {
          try{
            Thread.sleep(DELAY_BETWEEN_CHUNKS_MILLIS);
          }catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("색인 작업이 중단됐습니다");
            break;
          }
        }
      }

      log.info("Elasticsearch/Opensearch 색인 완료 - 전체{}건 완료", total);
    } catch (Exception e) {
      // ES 미가용 시 검색 기능만 비활성화하고 앱은 정상 기동 (연결 복구 후 재기동하면 재색인됨)
      log.warn("Elasticsearch/Opensearch 연결 실패로 콘텐츠 재색인을 건너뜁니다: {}", e.getMessage());
    }
  }
}