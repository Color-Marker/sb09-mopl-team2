package com.sb09.sb09moplteam2.batch.sport;

import static org.mockito.BDDMockito.then;

import com.sb09.sb09moplteam2.content.batch.sport.SportWriter;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
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
    Chunk<Content> chunk = new Chunk<>(List.of(content1, content2));

    SportWriter writer = new SportWriter(contentRepository);

    writer.write(chunk);

    then(contentRepository).should().saveAll(List.of(content1, content2));
  }

  @Test
  void 빈_청크는_빈_리스트로_saveAll을_호출한다() {
    Chunk<Content> chunk = new Chunk<>(List.of());
    SportWriter writer = new SportWriter(contentRepository);

    writer.write(chunk);

    then(contentRepository).should().saveAll(List.of());
  }
}