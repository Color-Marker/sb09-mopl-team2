package com.sb09.sb09moplteam2.content.service;


import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.dto.request.ContentCreateRequest;
import com.sb09.sb09moplteam2.content.dto.request.ContentUpdateRequest;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.mapper.ContentMapper;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.exception.content.ContentNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

  private final ContentRepository contentRepository;
  private final ContentTagRepository contentTagRepository;
  private final ContentMapper contentMapper;

  @Transactional
  public ContentDto create(ContentCreateRequest request) {
    log.info("콘텐츠 생성 요청 - type: {}, title: {}", request.type(), request.title());

    Content content = Content.builder()
        .type(request.type())
        .title(request.title())
        .description(request.description())
        .build();
    contentRepository.save(content);

    List<ContentTag> tags = request.tags().stream()
        .map(tag -> ContentTag.builder()
            .content(content)
            .tag(tag)
            .build())
        .toList();
    contentTagRepository.saveAll(tags);

    log.info("콘텐츠 생성 완료 - id: {}", content.getId());
    return contentMapper.toDto(content, tags);
  }

  public CursorResponseContentDto findContents(
      String typeEqual, String keywordLike, List<String> tagsIn,
      String cursor, UUID idAfter, Integer limit, String sortDirection, String sortBy
  ) {
    log.info("콘텐츠 목록 조회 - sortBy: {}, sortDirection: {}, limit: {}", sortBy, sortDirection, limit);
    return contentRepository.findContentsWithCursor(
        typeEqual, keywordLike, tagsIn, cursor, idAfter, limit, sortDirection, sortBy
    );
  }

  public ContentDto findById(UUID contentId) {
    log.info("콘텐츠 단건 조회 - contentId: {}", contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", contentId);
          return new ContentNotFoundException();
        });
    List<ContentTag> tags = contentTagRepository.findByContentId(contentId);
    return contentMapper.toDto(content, tags);
  }

  @Transactional
  public ContentDto update(UUID contentId, ContentUpdateRequest request) {
    log.info("콘텐츠 수정 요청 - contentId: {}", contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", contentId);
          return new ContentNotFoundException();
        });

    content.update(request.title(), request.description());

    if (request.tags() != null) {
      contentTagRepository.deleteByContentId(contentId);
      List<ContentTag> newTags = request.tags().stream()
          .map(tag -> ContentTag.builder()
              .content(content)
              .tag(tag)
              .build())
          .toList();
      contentTagRepository.saveAll(newTags);
    }

    List<ContentTag> tags = contentTagRepository.findByContentId(contentId);

    log.info("콘텐츠 수정 완료 - contentId: {}", contentId);
    return contentMapper.toDto(content, tags);
  }

  @Transactional
  public void delete(UUID contentId) {
    log.info("콘텐츠 삭제 요청 - contentId: {}", contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", contentId);
          return new ContentNotFoundException();
        });
    contentRepository.delete(content);
    log.info("콘텐츠 삭제 완료 - contentId: {}", contentId);
  }

  @Transactional(readOnly = true)
  public ContentSummary getContentSummary(UUID contentId) {
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", contentId);
          return new ContentNotFoundException();
        });
    List<ContentTag> tags = contentTagRepository.findByContentId(contentId);
    return contentMapper.toContentSummary(content, tags);
  }
}