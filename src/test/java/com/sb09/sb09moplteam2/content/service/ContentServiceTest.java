package com.sb09.sb09moplteam2.content.service;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.dto.request.ContentCreateRequest;
import com.sb09.sb09moplteam2.content.dto.request.ContentUpdateRequest;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.mapper.ContentMapper;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.exception.content.ContentNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

  @InjectMocks
  private ContentService contentService;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentTagRepository contentTagRepository;

  @Mock
  private ContentMapper contentMapper;

  @Test
  @DisplayName("콘텐츠 생성 성공")
  void 콘텐츠_생성에_성공하면_ContentDto를_반환한다() {
    ContentCreateRequest request = new ContentCreateRequest(
        ContentType.movie, "테스트 영화", "설명", List.of("액션", "SF")
    );

    given(contentRepository.save(any(Content.class))).willAnswer(invocation -> invocation.getArgument(0));
    given(contentTagRepository.saveAll(anyList())).willReturn(List.of());

    ContentDto expectedDto = new ContentDto(
        UUID.randomUUID(), ContentType.movie, "테스트 영화", "설명",
        null, List.of("액션", "SF"), 0.0, 0, 0L
    );
    given(contentMapper.toDto(any(Content.class), anyList())).willReturn(expectedDto);

    ContentDto result = contentService.create(request);

    assertThat(result.title()).isEqualTo("테스트 영화");
    verify(contentRepository).save(any(Content.class));
    verify(contentTagRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("콘텐츠 목록 조회 성공")
  void 콘텐츠_목록_조회에_성공하면_CursorResponseContentDto를_반환한다() {
    CursorResponseContentDto response = new CursorResponseContentDto(
        List.of(), null, null, false, 0L, "createdAt", "DESCENDING"
    );
    given(contentRepository.findContentsWithCursor(
        any(), any(), any(), any(), any(), anyInt(), any(), any()
    )).willReturn(response);

    CursorResponseContentDto result = contentService.findContents(
        "movie", "키워드", List.of("액션"), null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result.sortBy()).isEqualTo("createdAt");
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("콘텐츠 단건 조회 성공")
  void 콘텐츠_단건_조회에_성공하면_ContentDto를_반환한다() {
    UUID contentId = UUID.randomUUID();
    Content content = mock(Content.class);
    ContentDto expectedDto = new ContentDto(
        contentId, ContentType.movie, "테스트 영화", "설명",
        null, List.of(), 0.0, 0, 0L
    );

    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
    given(contentTagRepository.findByContentId(contentId)).willReturn(List.of());
    given(contentMapper.toDto(content, List.of())).willReturn(expectedDto);

    ContentDto result = contentService.findById(contentId);

    assertThat(result.id()).isEqualTo(contentId);
  }

  @Test
  @DisplayName("콘텐츠 단건 조회 실패 - 콘텐츠 없음")
  void 존재하지_않는_콘텐츠_조회시_예외를_던진다() {
    UUID contentId = UUID.randomUUID();
    given(contentRepository.findById(contentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> contentService.findById(contentId))
        .isInstanceOf(ContentNotFoundException.class);
  }

  @Test
  @DisplayName("콘텐츠 수정 성공")
  void 콘텐츠_수정에_성공하면_ContentDto를_반환한다() {
    UUID contentId = UUID.randomUUID();
    Content content = mock(Content.class);
    ContentUpdateRequest request = new ContentUpdateRequest("수정된 제목", "수정된 설명", List.of("드라마"));

    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
    given(contentTagRepository.findByContentId(contentId)).willReturn(List.of());
    ContentDto expectedDto = new ContentDto(
        contentId, ContentType.movie, "수정된 제목", "수정된 설명",
        null, List.of("드라마"), 0.0, 0, 0L
    );
    given(contentMapper.toDto(content, List.of())).willReturn(expectedDto);

    ContentDto result = contentService.update(contentId, request);

    assertThat(result.title()).isEqualTo("수정된 제목");
    verify(content).update("수정된 제목", "수정된 설명");
    verify(contentTagRepository).deleteByContentId(contentId);
    verify(contentTagRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("콘텐츠 수정 실패 - 콘텐츠 없음")
  void 존재하지_않는_콘텐츠_수정시_예외를_던진다() {
    UUID contentId = UUID.randomUUID();
    ContentUpdateRequest request = new ContentUpdateRequest("수정된 제목", "수정된 설명", List.of());

    given(contentRepository.findById(contentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> contentService.update(contentId, request))
        .isInstanceOf(ContentNotFoundException.class);
  }

  @Test
  @DisplayName("콘텐츠 삭제 성공")
  void 콘텐츠_삭제에_성공하면_콘텐츠가_삭제된다() {
    UUID contentId = UUID.randomUUID();
    Content content = mock(Content.class);
    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

    contentService.delete(contentId);

    verify(contentRepository).delete(content);
  }

  @Test
  @DisplayName("콘텐츠 삭제 실패 - 콘텐츠 없음")
  void 존재하지_않는_콘텐츠_삭제시_예외를_던진다() {
    UUID contentId = UUID.randomUUID();
    given(contentRepository.findById(contentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> contentService.delete(contentId))
        .isInstanceOf(ContentNotFoundException.class);
  }

  @Test
  @DisplayName("콘텐츠 요약 조회 성공")
  void 콘텐츠_요약_조회에_성공하면_ContentSummary를_반환한다() {
    UUID contentId = UUID.randomUUID();
    Content content = mock(Content.class);
    ContentSummary summary = new ContentSummary(
        contentId, ContentType.movie, "테스트 영화", "설명", null, List.of(), 0.0, 0
    );

    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
    given(contentTagRepository.findByContentId(contentId)).willReturn(List.of());
    given(contentMapper.toContentSummary(content, List.of())).willReturn(summary);

    ContentSummary result = contentService.getContentSummary(contentId);

    assertThat(result.title()).isEqualTo("테스트 영화");
  }

  @Test
  @DisplayName("콘텐츠 요약 조회 실패 - 콘텐츠 없음")
  void 존재하지_않는_콘텐츠_요약_조회시_예외를_던진다() {
    UUID contentId = UUID.randomUUID();
    given(contentRepository.findById(contentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> contentService.getContentSummary(contentId))
        .isInstanceOf(ContentNotFoundException.class);
  }
}