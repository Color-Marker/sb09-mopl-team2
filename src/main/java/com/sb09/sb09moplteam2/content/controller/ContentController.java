package com.sb09.sb09moplteam2.content.controller;

import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.dto.request.ContentCreateRequest;
import com.sb09.sb09moplteam2.content.dto.request.ContentUpdateRequest;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "콘텐츠 관리")
public class ContentController {

  private final ContentService contentService;

  @Operation(summary = "[어드민] 콘텐츠 생성")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ContentDto> create(
      @RequestPart("request") @Valid ContentCreateRequest request,
      @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
  ) {
    log.info("POST /api/contents - type: {}, title: {}", request.type(), request.title());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(contentService.create(request, thumbnail));
  }

  @Operation(summary = "콘텐츠 단건 조회")
  @GetMapping("/{contentId}")
  public ResponseEntity<ContentDto> findById(
      @PathVariable UUID contentId
  ) {
    log.info("GET /api/contents/{}", contentId);
    return ResponseEntity.ok(contentService.findById(contentId));
  }

  @Operation(summary = "[어드민] 콘텐츠 수정")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping(value = "/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ContentDto> update(
      @PathVariable UUID contentId,
      @RequestPart("request") @Valid ContentUpdateRequest request,
      @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
  ) {
    log.info("PATCH /api/contents/{}", contentId);
    return ResponseEntity.ok(contentService.update(contentId, request, thumbnail));
  }

  @Operation(summary = "[어드민] 콘텐츠 삭제")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{contentId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID contentId
  ) {
    log.info("DELETE /api/contents/{}", contentId);
    contentService.delete(contentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "콘텐츠 목록 조회(커서 페이지네이션)")
  @GetMapping
  public ResponseEntity<CursorResponseContentDto> findContents(
      @Parameter(description = "콘텐츠 타입", schema = @Schema(allowableValues = {"movie", "tvSeries", "sport"}))
      @RequestParam(required = false) String typeEqual,
      @Parameter(description = "검색 키워드") @RequestParam(required = false) String keywordLike,
      @Parameter(description = "태그 목록") @RequestParam(required = false) List<String> tagsIn,
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "보조 커서") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "한 번에 가져올 개수") @RequestParam @NotNull Integer limit,
      @Parameter(description = "정렬 방향", schema = @Schema(allowableValues = {"ASCENDING", "DESCENDING"}))
      @RequestParam @NotNull String sortDirection,
      @Parameter(description = "정렬 기준", schema = @Schema(allowableValues = {"createdAt", "watcherCount","rate"}))
      @RequestParam @NotNull String sortBy
  ) {
    log.info("GET /api/contents");
    return ResponseEntity.ok(contentService.findContents(
        typeEqual, keywordLike, tagsIn, cursor, idAfter, limit, sortDirection, sortBy
    ));
  }
}