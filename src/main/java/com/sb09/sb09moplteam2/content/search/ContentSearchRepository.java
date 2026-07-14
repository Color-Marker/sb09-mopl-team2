package com.sb09.sb09moplteam2.content.search;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContentSearchRepository extends ElasticsearchRepository<ContentDocument, String> {

  List<ContentDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}