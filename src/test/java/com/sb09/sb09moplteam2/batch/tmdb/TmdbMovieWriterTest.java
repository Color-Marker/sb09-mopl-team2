package com.sb09.sb09moplteam2.batch.tmdb;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbMovieWriter;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.util.List;
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

  @Test
  @DisplayName("콘텐츠 목록을 저장한다")
  void write_콘텐츠_목록을_저장한다() throws Exception {
    TmdbMovieWriter writer = new TmdbMovieWriter(contentRepository);
    Content content = mock(Content.class);
    Chunk<Content> chunk = new Chunk<>(List.of(content));

    writer.write(chunk);

    verify(contentRepository).saveAll(anyList());
  }
}