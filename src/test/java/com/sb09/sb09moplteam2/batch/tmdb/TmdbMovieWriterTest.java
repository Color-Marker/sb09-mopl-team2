package com.sb09.sb09moplteam2.batch.tmdb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieWriter;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class TmdbMovieWriterTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentTagRepository contentTagRepository;

  @Mock
  private ContentSearchService contentSearchService;

  @Test
  @DisplayName("콘텐츠 목록을 저장한다")
  void write_콘텐츠_목록을_저장한다() throws Exception {
    TmdbMovieWriter writer = new TmdbMovieWriter(contentRepository, contentTagRepository, contentSearchService);
    Content content = mock(Content.class);
    given(content.getId()).willReturn(UUID.randomUUID());
    given(content.getType()).willReturn(ContentType.movie);
    given(content.getTitle()).willReturn("테스트 영화");
    given(content.getDescription()).willReturn("설명");
    ContentAndTags item = new ContentAndTags(content, List.of());
    Chunk<ContentAndTags> chunk = new Chunk<>(List.of(item));

    writer.write(chunk);

    verify(contentRepository).saveAll(anyList());
    verify(contentTagRepository, never()).saveAll(anyList());
    verify(contentSearchService).indexAll(any(List.class));
  }

  @Test
  @DisplayName("태그가 있으면 ContentTag도 함께 저장한다")
  void write_태그가_있으면_ContentTag도_함께_저장한다() throws Exception {
    TmdbMovieWriter writer = new TmdbMovieWriter(contentRepository, contentTagRepository, contentSearchService);
    Content content = mock(Content.class);
    given(content.getId()).willReturn(UUID.randomUUID());
    given(content.getType()).willReturn(ContentType.movie);
    given(content.getTitle()).willReturn("테스트 영화");
    given(content.getDescription()).willReturn("설명");
    ContentAndTags item = new ContentAndTags(content, List.of("액션", "SF"));
    Chunk<ContentAndTags> chunk = new Chunk<>(List.of(item));

    writer.write(chunk);

    verify(contentRepository).saveAll(anyList());
    verify(contentTagRepository).saveAll(anyList());
    verify(contentSearchService).indexAll(any(List.class));
  }
}