package com.sb09.sb09moplteam2.content.batch.Tmdb;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class TmdbMovieWriter implements ItemWriter<Content> {

  private final ContentRepository contentRepository;

  @Override
  public void write(Chunk<? extends Content> chunk) {
    List<? extends Content> contents = chunk.getItems();
    contentRepository.saveAll(contents);
    log.info("콘텐츠 저장 완료 - {}건", contents.size());
  }
}