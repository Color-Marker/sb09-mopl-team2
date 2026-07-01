package com.sb09.sb09moplteam2.review.service;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
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
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    if (reviewRepository.existsByContentIdAndUserId(request.contentId(), currentUserId)) {
      throw new IllegalArgumentException("이미 리뷰를 작성했습니다.");
    }
    Content content = contentRepository.findById(request.contentId())
        .orElseThrow(() -> new NoSuchElementException("콘텐츠를 찾을 수 없습니다."));
    User user = userRepository.findById(currentUserId)
        .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
    Review review = Review.builder()
        .rating(request.rating())
        .text(request.text())
        .content(content)
        .user(user)
        .build();
    reviewRepository.save(review);
    return reviewMapper.toDto(review);
  }

  @Transactional
  public ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID currentUserId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NoSuchElementException("리뷰를 찾을 수 없습니다."));
    if (!review.getUser().getId().equals(currentUserId)) {
      throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
    }
    review.update(request.rating(), request.text());
    return reviewMapper.toDto(review);
  }

  @Transactional
  public void delete(UUID reviewId, UUID currentUserId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NoSuchElementException("리뷰를 찾을 수 없습니다."));
    if (!review.getUser().getId().equals(currentUserId)) {
      throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다.");
    }
    reviewRepository.delete(review);
  }

  public CursorResponseReviewDto findAll(
      UUID contentId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  ) {
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

    return new CursorResponseReviewDto(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        (long) data.size(),
        sortBy,
        sortDirection
    );
  }
}