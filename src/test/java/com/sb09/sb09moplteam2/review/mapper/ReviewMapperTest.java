package com.sb09.sb09moplteam2.review.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.review.dto.data.ReviewDto;
import com.sb09.sb09moplteam2.review.entity.Review;
import com.sb09.sb09moplteam2.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

  private final ReviewMapper reviewMapper = new ReviewMapper();

  @Mock
  private Review review;

  @Mock
  private Content content;

  @Mock
  private User user;

  @Test
  @DisplayName("toDto - Review를 ReviewDto로 변환한다")
  void toDto_Review를_ReviewDto로_변환한다() {
    UUID reviewId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(review.getId()).willReturn(reviewId);
    given(review.getContent()).willReturn(content);
    given(content.getId()).willReturn(contentId);
    given(review.getUser()).willReturn(user);
    given(user.getId()).willReturn(userId);
    given(user.getName()).willReturn("홍길동");
    given(user.getProfileImageUrl()).willReturn("/files/profile.jpg");
    given(review.getText()).willReturn("정말 재미있는 영화였습니다.");
    given(review.getRating()).willReturn(4.5);

    ReviewDto result = reviewMapper.toDto(review);

    assertThat(result.id()).isEqualTo(reviewId);
    assertThat(result.contentId()).isEqualTo(contentId);
    assertThat(result.author().userId()).isEqualTo(userId);
    assertThat(result.author().name()).isEqualTo("홍길동");
    assertThat(result.author().profileImageUrl()).isEqualTo("/files/profile.jpg");
    assertThat(result.text()).isEqualTo("정말 재미있는 영화였습니다.");
    assertThat(result.rating()).isEqualTo(4.5);
  }

  @Test
  @DisplayName("toDto - 프로필 이미지가 없으면 null로 변환한다")
  void toDto_프로필_이미지가_없으면_null로_변환한다() {
    given(review.getId()).willReturn(UUID.randomUUID());
    given(review.getContent()).willReturn(content);
    given(content.getId()).willReturn(UUID.randomUUID());
    given(review.getUser()).willReturn(user);
    given(user.getId()).willReturn(UUID.randomUUID());
    given(user.getName()).willReturn("김철수");
    given(user.getProfileImageUrl()).willReturn(null);
    given(review.getText()).willReturn("괜찮았어요");
    given(review.getRating()).willReturn(3.0);

    ReviewDto result = reviewMapper.toDto(review);

    assertThat(result.author().profileImageUrl()).isNull();
  }
}