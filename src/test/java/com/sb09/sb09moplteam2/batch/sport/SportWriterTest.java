package com.sb09.sb09moplteam2.batch.sport;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sb09.sb09moplteam2.content.batch.ContentAndTags;
import com.sb09.sb09moplteam2.content.batch.sport.SportWriter;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class SportWriterTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentTagRepository contentTagRepository;

  @Test
  void 청크의_콘텐츠를_저장한다() {
    Content content1 = Content.builder()
        .type(ContentType.sport)
        .externalId("1")
        .title("경기1")
        .build();
    Content content2 = Content.builder()
        .type(ContentType.sport)
        .externalId("2")
        .title("경기2")
        .build();
    ContentAndTags item1 = new ContentAndTags(content1, List.of());
    ContentAndTags item2 = new ContentAndTags(content2, List.of());
    Chunk<ContentAndTags> chunk = new Chunk<>(List.of(item1, item2));

    SportWriter writer = new SportWriter(contentRepository, contentTagRepository);

    writer.write(chunk);

    then(contentRepository).should().saveAll(anyList());
    then(contentTagRepository).should(never()).saveAll(anyList());
  }

  @Test
  void 태그가_있으면_ContentTag도_함께_저장한다() {
    Content content = Content.builder()
        .type(ContentType.sport)
        .externalId("1")
        .title("경기1")
        .build();
    ContentAndTags item = new ContentAndTags(content, List.of("축구"));
    Chunk<ContentAndTags> chunk = new Chunk<>(List.of(item));

    SportWriter writer = new SportWriter(contentRepository, contentTagRepository);

    writer.write(chunk);

    then(contentRepository).should().saveAll(anyList());
    then(contentTagRepository).should().saveAll(anyList());
  }

  @Test
  void 빈_청크는_빈_리스트로_saveAll을_호출한다() {
    Chunk<ContentAndTags> chunk = new Chunk<>(List.of());
    SportWriter writer = new SportWriter(contentRepository, contentTagRepository);

    writer.write(chunk);

    then(contentRepository).should().saveAll(List.of());
    then(contentTagRepository).should(never()).saveAll(anyList());
  }
}