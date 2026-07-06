package com.sb09.sb09moplteam2.review.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.exception.content.ContentNotFoundException;
import com.sb09.sb09moplteam2.exception.review.DuplicateReviewException;
import com.sb09.sb09moplteam2.exception.review.ReviewForbiddenException;
import com.sb09.sb09moplteam2.exception.review.ReviewNotFoundException;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @InjectMocks
  private ReviewService reviewService;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReviewMapper reviewMapper;

  @Test
  @DisplayName("리뷰 생성 성공")
  void 리뷰_생성에_성공하면_ReviewDto를_반환한다() {
    UUID userId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    User user = mock(User.class);
    Content content = mock(Content.class);
    ReviewCreateRequest request = new ReviewCreateRequest(contentId, "좋아요", 4.5);
    ReviewDto reviewDto = new ReviewDto(reviewId, contentId, null, "좋아요", 4.5);

    given(reviewRepository.existsByContentIdAndUserId(contentId, userId)).willReturn(false);
    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));
    given(reviewMapper.toDto(any(Review.class))).willReturn(reviewDto);

    ReviewDto result = reviewService.create(request, userId);

    assertThat(result.contentId()).isEqualTo(contentId);
    assertThat(result.text()).isEqualTo("좋아요");
    assertThat(result.rating()).isEqualTo(4.5);
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  @DisplayName("리뷰 생성 실패 - 중복 리뷰")
  void 이미_리뷰를_작성한_경우_예외를_던진다() {
    UUID userId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    ReviewCreateRequest request = new ReviewCreateRequest(contentId, "좋아요", 4.5);

    given(reviewRepository.existsByContentIdAndUserId(contentId, userId)).willReturn(true);

    assertThatThrownBy(() -> reviewService.create(request, userId))
        .isInstanceOf(DuplicateReviewException.class);
  }

  @Test
  @DisplayName("리뷰 생성 실패 - 콘텐츠 없음")
  void 존재하지_않는_콘텐츠에_리뷰_생성시_예외를_던진다() {
    UUID userId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    ReviewCreateRequest request = new ReviewCreateRequest(contentId, "좋아요", 4.5);

    given(reviewRepository.existsByContentIdAndUserId(contentId, userId)).willReturn(false);
    given(contentRepository.findById(contentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.create(request, userId))
        .isInstanceOf(ContentNotFoundException.class);
  }

  @Test
  @DisplayName("리뷰 수정 성공")
  void 리뷰수정에_성공하면_ReviewDto를_반환한다() {
    UUID userId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Content content = mock(Content.class);
    given(content.getId()).willReturn(contentId);

    Review review = mock(Review.class);
    given(review.getUser()).willReturn(user);
    given(review.getContent()).willReturn(content);

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 리뷰", 3.0);
    ReviewDto reviewDto = new ReviewDto(reviewId, contentId, null, "수정된 리뷰", 3.0);

    given(reviewRepository.findByContentId(contentId)).willReturn(List.of());
    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(reviewMapper.toDto(review)).willReturn(reviewDto);

    ReviewDto result = reviewService.update(reviewId, request, userId);

    assertThat(result.text()).isEqualTo("수정된 리뷰");
    assertThat(result.rating()).isEqualTo(3.0);
    verify(review).update(3.0, "수정된 리뷰");
  }

  @Test
  @DisplayName("리뷰 수정 실패 - 권한 없음")
  void 다른사용자의_리뷰_수정시_예외를_던진다() {
    UUID userId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Review review = mock(Review.class);
    given(review.getUser()).willReturn(user);

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 리뷰", 3.0);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> reviewService.update(reviewId, request, otherUserId))
        .isInstanceOf(ReviewForbiddenException.class);
  }

  @Test
  @DisplayName("리뷰 수정 실패 - 리뷰 없음")
  void 존재하지_않는_리뷰_수정시_예외를_던진다() {
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 리뷰", 3.0);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.update(reviewId, request, userId))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  @DisplayName("리뷰 삭제 성공")
  void 리뷰_삭제에_성공하면_리뷰가_삭제된다() {
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Content content = mock(Content.class);
    given(content.getId()).willReturn(contentId);

    Review review = mock(Review.class);
    given(review.getUser()).willReturn(user);
    given(review.getContent()).willReturn(content);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(reviewRepository.findByContentId(contentId)).willReturn(List.of());
    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

    reviewService.delete(reviewId, userId);

    verify(reviewRepository).delete(review);
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 권한 없음")
  void 다른_사용자의_리뷰_삭제시_예외를_던진다() {
    UUID userId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Review review = mock(Review.class);
    given(review.getUser()).willReturn(user);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> reviewService.delete(reviewId, otherUserId))
        .isInstanceOf(ReviewForbiddenException.class);
  }

  @Test
  @DisplayName("리뷰 삭제 실패 - 리뷰 없음")
  void 존재하지_않는_리뷰_삭제시_예외를_던진다() {
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.delete(reviewId, userId))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  @DisplayName("리뷰 목록 조회 성공")
  void 리뷰_목록_조회에_성공하면_CursorResponseReviewDto를_반환한다() {
    UUID contentId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    Review review = mock(Review.class);
    ReviewDto reviewDto = new ReviewDto(reviewId, contentId, null, "좋아요", 4.5);

    given(reviewRepository.findReviewsWithCursor(
        any(), any(), any(), anyInt(), any(), any()
    )).willReturn(List.of(review));
    given(reviewMapper.toDto(review)).willReturn(reviewDto);

    CursorResponseReviewDto result = reviewService.findAll(
        contentId, null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.sortBy()).isEqualTo("createdAt");
  }
}