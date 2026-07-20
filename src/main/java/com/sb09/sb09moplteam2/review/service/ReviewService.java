package com.sb09.sb09moplteam2.review.service;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.exception.content.ContentNotFoundException;
import com.sb09.sb09moplteam2.exception.review.DuplicateReviewException;
import com.sb09.sb09moplteam2.exception.review.ReviewForbiddenException;
import com.sb09.sb09moplteam2.exception.review.ReviewNotFoundException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.review.dto.data.ReviewDto;
import com.sb09.sb09moplteam2.review.dto.request.ReviewCreateRequest;
import com.sb09.sb09moplteam2.review.dto.request.ReviewUpdateRequest;
import com.sb09.sb09moplteam2.review.dto.response.CursorResponseReviewDto;
import com.sb09.sb09moplteam2.review.entity.Review;
import com.sb09.sb09moplteam2.review.mapper.ReviewMapper;
import com.sb09.sb09moplteam2.review.repository.ReviewRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
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
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ContentRepository contentRepository;
  private final UserRepository userRepository;
  private final ReviewMapper reviewMapper;

  @Transactional
  public ReviewDto create(ReviewCreateRequest request, UUID currentUserId) {
    log.info("리뷰 생성 요청 - contentId: {}, userId: {}", request.contentId(), currentUserId);
    if (reviewRepository.existsByContentIdAndUserId(request.contentId(), currentUserId)) {
      throw new DuplicateReviewException();
    }
    Content content = contentRepository.findById(request.contentId())
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", request.contentId());
          return new ContentNotFoundException();
        });
    User user = userRepository.findById(currentUserId)
        .orElseThrow(() -> UserNotFoundException.withId(currentUserId));
    Review review = Review.builder()
        .rating(request.rating())
        .text(request.text())
        .content(content)
        .user(user)
        .build();
    reviewRepository.save(review);
    updateContentReviewStats(request.contentId());
    log.info("리뷰 생성 완료 - reviewId: {}", review.getId());
    return reviewMapper.toDto(review);
  }

  @Transactional
  public ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID currentUserId) {
    log.info("리뷰 수정 요청 - reviewId: {}", reviewId);
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> {
          log.warn("리뷰 없음 - reviewId: {}", reviewId);
          return new ReviewNotFoundException();
        });
    if (!review.getUser().getId().equals(currentUserId)) {
      throw new ReviewForbiddenException();
    }
    review.update(request.rating(), request.text());
    updateContentReviewStats(review.getContent().getId());
    log.info("리뷰 수정 완료 - reviewId: {}", reviewId);
    return reviewMapper.toDto(review);
  }

  @Transactional
  public void delete(UUID reviewId, UUID currentUserId) {
    log.info("리뷰 삭제 요청 - reviewId: {}", reviewId);
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> {
          log.warn("리뷰 없음 - reviewId: {}", reviewId);
          return new ReviewNotFoundException();
        });
    if (!review.getUser().getId().equals(currentUserId)) {
      throw new ReviewForbiddenException();
    }
    reviewRepository.delete(review);
    updateContentReviewStats(review.getContent().getId());
    log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);
  }

  public CursorResponseReviewDto findAll(
      UUID contentId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  ) {
    log.info("리뷰 목록 조회 - contentId: {}, sortBy: {}, sortDirection: {}, limit: {}", contentId, sortBy, sortDirection, limit);
    List<Review> reviews = reviewRepository.findReviewsWithCursor(
        contentId, cursor, idAfter, limit, sortDirection, sortBy
    );

    boolean hasNext = reviews.size() > limit;
    List<Review> content = hasNext ? reviews.subList(0, limit) : reviews;

    List<ReviewDto> data = content.stream()
        .map(reviewMapper::toDto)
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      Review last = content.get(content.size() - 1);
      nextCursor = "createdAt".equals(sortBy)
          ? last.getCreatedAt().toString()
          : String.valueOf(last.getRating());
      nextIdAfter = last.getId();
    }
    Long totalCount = (idAfter == null) ? reviewRepository.countByContentId(contentId) : null;

    log.info("리뷰 목록 조회 완료 - 총 {}개", totalCount);
    return new CursorResponseReviewDto(
        data, nextCursor, nextIdAfter, hasNext, totalCount, sortBy, sortDirection
    );
  }

  private void updateContentReviewStats(UUID contentId) {
    List<Review> reviews = reviewRepository.findByContentId(contentId);
    int reviewCount = reviews.size();
    double averageRating = reviews.stream()
        .mapToDouble(Review::getRating)
        .average()
        .orElse(0.0);
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", contentId);
          return new ContentNotFoundException();
        });
    content.updateReviewStats(averageRating, reviewCount);
  }
}