package com.sb09.sb09moplteam2.content.batch.sport;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class SportWriter implements ItemWriter<Content> {

  private final ContentRepository contentRepository;

  @Override
  public void write(Chunk<? extends Content> chunk) {
    List<? extends Content> items = chunk.getItems();
    contentRepository.saveAll(items);
    log.info("스포츠 콘텐츠 {}개 저장 완료", items.size());
  }
}