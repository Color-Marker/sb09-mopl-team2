package com.sb09.sb09moplteam2.content.controller;

import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.dto.request.ContentCreateRequest;
import com.sb09.sb09moplteam2.content.dto.request.ContentUpdateRequest;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.service.ContentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

  private final ContentService contentService;

  @PostMapping
  public ResponseEntity<ContentDto> create(
      @RequestBody @Valid ContentCreateRequest request
  ) {
    log.info("POST /api/contents - type: {}, title: {}", request.type(), request.title());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(contentService.create(request));
  }

  @GetMapping("/{contentId}")
  public ResponseEntity<ContentDto> findById(
      @PathVariable UUID contentId
  ) {
    log.info("GET /api/contents/{}", contentId);
    return ResponseEntity.ok(contentService.findById(contentId));
  }

  @PatchMapping("/{contentId}")
  public ResponseEntity<ContentDto> update(
      @PathVariable UUID contentId,
      @RequestBody @Valid ContentUpdateRequest request
  ) {
    log.info("PATCH /api/contents/{}", contentId);
    return ResponseEntity.ok(contentService.update(contentId, request));
  }

  @DeleteMapping("/{contentId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID contentId
  ) {
    log.info("DELETE /api/contents/{}", contentId);
    contentService.delete(contentId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<CursorResponseContentDto> findContents(
      @RequestParam(required = false) String typeEqual,
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) List<String> tagsIn,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam @NotNull Integer limit,
      @RequestParam @NotNull String sortDirection,
      @RequestParam @NotNull String sortBy
  ) {
    log.info("GET /api/contents");
    return ResponseEntity.ok(contentService.findContents(
        typeEqual, keywordLike, tagsIn, cursor, idAfter, limit, sortDirection, sortBy
    ));
  }
}