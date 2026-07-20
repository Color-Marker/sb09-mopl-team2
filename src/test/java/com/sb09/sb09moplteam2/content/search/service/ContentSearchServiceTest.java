package com.sb09.sb09moplteam2.content.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sb09.sb09moplteam2.content.search.ContentDocument;
import com.sb09.sb09moplteam2.content.search.ContentSearchRepository;
import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentSearchServiceTest {

  @Mock
  private ContentSearchRepository contentSearchRepository;

  @InjectMocks
  private ContentSearchService contentSearchService;

  @Test
  @DisplayName("콘텐츠 문서를 색인한다")
  void index_콘텐츠_문서를_색인한다() {
    ContentDocument document = ContentDocument.builder()
        .id(UUID.randomUUID().toString())
        .title("테스트 영화")
        .description("설명")
        .type("movie")
        .build();

    contentSearchService.index(document);

    then(contentSearchRepository).should().save(document);
  }

  @Test
  @DisplayName("콘텐츠 문서를 삭제한다")
  void delete_콘텐츠_문서를_삭제한다() {
    UUID contentId = UUID.randomUUID();

    contentSearchService.delete(contentId);

    then(contentSearchRepository).should().deleteById(contentId.toString());
  }

  @Test
  @DisplayName("검색어와 일치하는 콘텐츠의 ID 목록을 반환한다")
  void searchIds_검색어와_일치하는_ID_목록을_반환한다() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    ContentDocument doc1 = ContentDocument.builder()
        .id(id1.toString())
        .title("어벤져스")
        .description("설명1")
        .type("movie")
        .build();
    ContentDocument doc2 = ContentDocument.builder()
        .id(id2.toString())
        .title("스파이더맨")
        .description("설명2")
        .type("movie")
        .build();

    given(contentSearchRepository.searchByKeyword("어벤져스"))
        .willReturn(List.of(doc1, doc2));

    List<UUID> result = contentSearchService.searchIds("어벤져스");

    assertThat(result).containsExactly(id1, id2);
  }

  @Test
  @DisplayName("일치하는 콘텐츠가 없으면 빈 목록을 반환한다")
  void searchIds_일치하는_콘텐츠가_없으면_빈_목록을_반환한다() {
    given(contentSearchRepository.searchByKeyword("없는검색어"))
        .willReturn(List.of());

    List<UUID> result = contentSearchService.searchIds("없는검색어");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("검색어 끝에 공백이 있으면 trim 후 검색한다")
  void searchIds_검색어_끝에_공백이_있으면_trim_후_검색한다() {
    given(contentSearchRepository.searchByKeyword("미국"))
        .willReturn(List.of());

    List<UUID> result = contentSearchService.searchIds("미국 ");

    assertThat(result).isEmpty();
    then(contentSearchRepository).should().searchByKeyword("미국");
  }

  @Test
  @DisplayName("검색어가 공백만 있으면 ES 호출 없이 빈 목록을 반환한다")
  void searchIds_검색어가_공백만_있으면_ES_호출없이_빈_목록을_반환한다() {
    List<UUID> result = contentSearchService.searchIds("   ");

    assertThat(result).isEmpty();
    then(contentSearchRepository).should(never()).searchByKeyword(any());
  }

  @Test
  @DisplayName("검색어가 null이면 ES 호출 없이 빈 목록을 반환한다")
  void searchIds_검색어가_null이면_ES_호출없이_빈_목록을_반환한다() {
    List<UUID> result = contentSearchService.searchIds(null);

    assertThat(result).isEmpty();
    then(contentSearchRepository).should(never()).searchByKeyword(any());
  }
}