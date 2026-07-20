package com.sb09.sb09moplteam2.content.search.initializer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.search.ContentDocument;
import com.sb09.sb09moplteam2.content.search.ContentSearchInitializer;
import com.sb09.sb09moplteam2.content.search.ContentSearchRepository;
import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@ExtendWith(MockitoExtension.class)
class ContentSearchInitializerTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentSearchService contentSearchService;

  @Mock
  private ElasticsearchOperations elasticsearchOperations;

  @Mock
  private IndexOperations indexOperations;

  @InjectMocks
  private ContentSearchInitializer contentSearchInitializer;

  @Mock
  private ContentSearchRepository contentSearchRepository;

  @BeforeEach
  void setUp() {
    given(elasticsearchOperations.indexOps(ContentDocument.class)).willReturn(indexOperations);
    given(indexOperations.exists()).willReturn(true);
  }

  @Test
  @DisplayName("색인된 데이터가 없으면 전체 콘텐츠를 색인한다")
  void reindexAll_색인된_데이터가_없으면_전체_콘텐츠를_색인한다() {
    given(contentSearchRepository.count()).willReturn(0L);

    Content content1 = mock(Content.class);
    given(content1.getId()).willReturn(UUID.randomUUID());
    given(content1.getType()).willReturn(ContentType.movie);
    given(content1.getTitle()).willReturn("영화1");

    Content content2 = mock(Content.class);
    given(content2.getId()).willReturn(UUID.randomUUID());
    given(content2.getType()).willReturn(ContentType.movie);
    given(content2.getTitle()).willReturn("영화2");

    given(contentRepository.findAll()).willReturn(List.of(content1, content2));

    contentSearchInitializer.reindexAll();

    then(contentRepository).should().findAll();
    then(contentSearchService).should(times(2)).index(any(ContentDocument.class));
  }

  @Test
  @DisplayName("콘텐츠가 없으면 색인을 호출하지 않는다")
  void reindexAll_콘텐츠가_없으면_색인하지_않는다() {
    given(contentRepository.findAll()).willReturn(List.of());

    contentSearchInitializer.reindexAll();

    then(contentSearchService).should(times(0)).index(any(ContentDocument.class));
  }
}