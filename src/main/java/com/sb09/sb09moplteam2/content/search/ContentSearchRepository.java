package com.sb09.sb09moplteam2.content.search;

import java.util.List;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContentSearchRepository extends ElasticsearchRepository<ContentDocument, String> {

  List<ContentDocument> findByTitleContainingOrDescriptionContaining(String title, String description);

  @Query("""
      {
        "bool": {
          "should": [
            { "match": { "title": "?0" } },
            { "match": { "description": "?0" } },
            { "term": { "tags": "?0" } }
          ]
        }
      }
      """)
  List<ContentDocument> searchByKeyword(String keyword);
}