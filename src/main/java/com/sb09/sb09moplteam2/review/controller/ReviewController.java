package com.sb09.sb09moplteam2.review.controller;

import com.sb09.sb09moplteam2.review.dto.data.ReviewDto;
import com.sb09.sb09moplteam2.review.dto.request.ReviewCreateRequest;
import com.sb09.sb09moplteam2.review.dto.request.ReviewUpdateRequest;
import com.sb09.sb09moplteam2.review.dto.response.CursorResponseReviewDto;
import com.sb09.sb09moplteam2.review.service.ReviewService;
import com.sb09.sb09moplteam2.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 관리")
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "리뷰 생성")
  @PostMapping
  public ResponseEntity<ReviewDto> create(
      @RequestBody ReviewCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ReviewDto dto = reviewService.create(request, userDetails.getId());
    log.info("POST /api/reviews - userId: {}", userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @Operation(summary = "리뷰 수정")
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> update(
      @PathVariable UUID reviewId,
      @RequestBody ReviewUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("PATCH /api/reviews/{} - userId: {}", reviewId, userDetails.getId());
    return ResponseEntity.ok(reviewService.update(reviewId, request, userDetails.getId()));
  }

  @Operation(summary = "리뷰 삭제")
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID reviewId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    reviewService.delete(reviewId, userDetails.getId());
    log.info("DELETE /api/reviews/{} - userId: {}", reviewId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "리뷰 목록 조회 (커서 페이지네이션)")
  @GetMapping
  public ResponseEntity<CursorResponseReviewDto> findAll(
      @Parameter(description = "콘텐츠 ID") @RequestParam(required = false) UUID contentId,
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "보조 커서") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "한 번에 가져올 개수") @RequestParam int limit,
      @Parameter(description = "정렬 방향", schema = @Schema(allowableValues = {"ASCENDING", "DESCENDING"}))
      @RequestParam String sortDirection,
      @Parameter(description = "정렬 기준", schema = @Schema(allowableValues = {"createdAt", "rating"}))
      @RequestParam String sortBy) {
    log.info("GET /api/reviews - contentId: {}, sortBy: {}", contentId, sortBy);
    return ResponseEntity.ok(reviewService.findAll(
        contentId, cursor, idAfter, limit, sortDirection, sortBy
    ));
  }
}