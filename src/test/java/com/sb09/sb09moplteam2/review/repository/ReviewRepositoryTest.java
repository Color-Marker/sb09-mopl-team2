package com.sb09.sb09moplteam2.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.config.MockSearchTestConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.review.entity.Review;

import com.sb09.sb09moplteam2.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class, TestJpaConfig.class, MockSearchTestConfig.class})
class ReviewRepositoryTest {

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private TestEntityManager em;

  @Test
  @DisplayName("리뷰가 존재하면 true를 반환한다")
  void existsByContentIdAndUserId_리뷰가_존재하면_true를_반환한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.persist(Review.builder()
        .rating(4.5)
        .text("좋아요")
        .content(content)
        .user(user)
        .build());
    em.flush();

    boolean result = reviewRepository.existsByContentIdAndUserId(content.getId(), user.getId());

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("리뷰가 없으면 false를 반환한다")
  void existsByContentIdAndUserId_리뷰가_없으면_false를_반환한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.flush();

    boolean result = reviewRepository.existsByContentIdAndUserId(content.getId(), user.getId());

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("커서 없이 createdAt 내림차순으로 조회한다")
  void findReviewsWithCursor_커서없이_createdAt_내림차순으로_조회한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.persist(Review.builder().rating(3.0).text("리뷰1").content(content).user(user).build());
    em.persist(Review.builder().rating(4.0).text("리뷰2").content(content).user(user).build());
    em.flush();

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("contentId로 필터링하여 해당 콘텐츠의 리뷰만 조회한다")
  void findReviewsWithCursor_contentId로_필터링하여_해당_콘텐츠의_리뷰만_조회한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    Content otherContent = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("다른 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.persist(Review.builder().rating(3.0).text("리뷰1").content(content).user(user).build());
    em.persist(Review.builder().rating(4.0).text("리뷰2").content(otherContent).user(user).build());
    em.flush();

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getText()).isEqualTo("리뷰1");
  }

  @Test
  @DisplayName("limit+1개를 조회하여 hasNext를 판단할 수 있다")
  void findReviewsWithCursor_limit초과시_limit_plus_1개를_반환한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    for (int i = 0; i < 5; i++) {
      em.persist(Review.builder()
          .rating(3.0).text("리뷰" + i).content(content).user(user).build());
    }
    em.flush();

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 3, "DESCENDING", "createdAt"
    );

    assertThat(result).hasSize(4);
  }

  @Test
  @DisplayName("rating 오름차순으로 정렬하여 조회한다")
  void findReviewsWithCursor_rating_오름차순으로_정렬하여_조회한다() {

    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.persist(Review.builder().rating(5.0).text("리뷰1").content(content).user(user).build());
    em.persist(Review.builder().rating(2.0).text("리뷰2").content(content).user(user).build());
    em.persist(Review.builder().rating(3.0).text("리뷰3").content(content).user(user).build());
    em.flush();

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 10, "ASCENDING", "rating"
    );

    assertThat(result.get(0).getRating()).isEqualTo(2.0);
    assertThat(result.get(1).getRating()).isEqualTo(3.0);
    assertThat(result.get(2).getRating()).isEqualTo(5.0);
  }

  @Test
  @DisplayName("createdAt 내림차순 커서 기준으로 다음 페이지를 조회한다")
  void findReviewsWithCursor_createdAt_내림차순_커서로_다음_페이지를_조회한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    Review review1 = em.persist(Review.builder().rating(3.0).text("리뷰1").content(content).user(user).build());
    em.flush();
    Review review2 = em.persist(Review.builder().rating(4.0).text("리뷰2").content(content).user(user).build());
    em.flush();

    List<Review> firstPage = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 1, "DESCENDING", "createdAt"
    );
    Review last = firstPage.get(0);

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), last.getCreatedAt().toString(), last.getId(), 10, "DESCENDING", "createdAt"
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getText()).isEqualTo("리뷰1");
  }

  @Test
  @DisplayName("createdAt 오름차순 커서 기준으로 다음 페이지를 조회한다")
  void findReviewsWithCursor_createdAt_오름차순_커서로_다음_페이지를_조회한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    Review review1 = em.persist(Review.builder().rating(3.0).text("리뷰1").content(content).user(user).build());
    em.flush();
    Review review2 = em.persist(Review.builder().rating(4.0).text("리뷰2").content(content).user(user).build());
    em.flush();

    List<Review> firstPage = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 1, "ASCENDING", "createdAt"
    );
    Review last = firstPage.get(0);

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), last.getCreatedAt().toString(), last.getId(), 10, "ASCENDING", "createdAt"
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getText()).isEqualTo("리뷰2");
  }

  @Test
  @DisplayName("rating 내림차순 커서 기준으로 다음 페이지를 조회한다")
  void findReviewsWithCursor_rating_내림차순_커서로_다음_페이지를_조회한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.persist(Review.builder().rating(5.0).text("리뷰1").content(content).user(user).build());
    em.persist(Review.builder().rating(3.0).text("리뷰2").content(content).user(user).build());
    em.flush();

    List<Review> firstPage = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 1, "DESCENDING", "rating"
    );
    Review last = firstPage.get(0);

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), String.valueOf(last.getRating()), last.getId(), 10, "DESCENDING", "rating"
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getText()).isEqualTo("리뷰2");
  }

  @Test
  @DisplayName("rating 오름차순 커서 기준으로 다음 페이지를 조회한다")
  void findReviewsWithCursor_rating_오름차순_커서로_다음_페이지를_조회한다() {
    User user = em.persist(new User("테스터", "test@test.com", "password"));
    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .title("테스트 영화")
        .description("설명")
        .externalId("test-external-id")
        .build());
    em.persist(Review.builder().rating(3.0).text("리뷰1").content(content).user(user).build());
    em.persist(Review.builder().rating(5.0).text("리뷰2").content(content).user(user).build());
    em.flush();

    List<Review> firstPage = reviewRepository.findReviewsWithCursor(
        content.getId(), null, null, 1, "ASCENDING", "rating"
    );
    Review last = firstPage.get(0);

    List<Review> result = reviewRepository.findReviewsWithCursor(
        content.getId(), String.valueOf(last.getRating()), last.getId(), 10, "ASCENDING", "rating"
    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getText()).isEqualTo("리뷰2");
  }
}