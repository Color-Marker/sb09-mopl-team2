package com.sb09.sb09moplteam2.content.search;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentSearchService {

  private final ContentSearchRepository contentSearchRepository;

  public void index(ContentDocument document) {
    contentSearchRepository.save(document);
  }

  public void indexAll(List<ContentDocument> documents) {
    contentSearchRepository.saveAll(documents);
  }

  public void delete(UUID contentId) {
    contentSearchRepository.deleteById(contentId.toString());
  }

  public List<UUID> searchIds(String keyword) {
    if(keyword == null) {
      return List.of();
    }
    String trimmed = keyword.trim();
    if (trimmed.isEmpty()) {
      return List.of();
    }
    return contentSearchRepository
        .searchByKeyword(trimmed)
        .stream()
        .map(doc -> UUID.fromString(doc.getId()))
        .toList();
  }
}