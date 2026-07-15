package com.sb09.sb09moplteam2.content.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import({QuerydslConfig.class, TestJpaConfig.class})
class ContentRepositoryTest {

  @Autowired
  private ContentRepository contentRepository;

  @Autowired
  private ContentTagRepository contentTagRepository;

  @Autowired
  private TestEntityManager em;

  @MockitoBean
  private ContentSearchService contentSearchService;

  @Test
  @DisplayName("type과 externalId로 콘텐츠를 조회한다")
  void findByTypeAndExternalId_존재하면_콘텐츠를_반환한다() {

    Content content = em.persist(Content.builder()
        .type(ContentType.movie)
        .externalId("ext-001")
        .title("테스트 영화")
        .description("설명")
        .build());
    em.flush();

    Optional<Content> result = contentRepository.findByTypeAndExternalId(ContentType.movie, "ext-001");

    assertThat(result).isPresent();
    assertThat(result.get().getTitle()).isEqualTo("테스트 영화");
  }

  @Test
  @DisplayName("존재하지 않는 type과 externalId로 조회하면 빈 값을 반환한다")
  void findByTypeAndExternalId_없으면_빈값을_반환한다() {
    Optional<Content> result = contentRepository.findByTypeAndExternalId(ContentType.movie, "not-exist");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("커서 없이 createdAt 내림차순으로 조회한다")
  void findContentsWithCursor_커서없이_createdAt_내림차순으로_조회한다() {
    em.persist(Content.builder().type(ContentType.movie).externalId("ext-001").title("영화1").description("설명").build());
    em.persist(Content.builder().type(ContentType.movie).externalId("ext-002").title("영화2").description("설명").build());
    em.flush();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, null, null, null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("typeEqual 필터로 특정 타입만 조회한다")
  void findContentsWithCursor_typeEqual_필터로_특정_타입만_조회한다() {
    em.persist(Content.builder().type(ContentType.movie).externalId("ext-001").title("영화1").description("설명").build());
    em.persist(Content.builder().type(ContentType.tvSeries).externalId("ext-002").title("드라마1").description("설명").build());
    em.flush();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        "movie", null, null, null, null, 10, "DESCENDING", "createdAt"
    );
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).title()).isEqualTo("영화1");
  }

  @Test
  @DisplayName("keywordLike 필터로 제목 검색이 된다")
  void findContentsWithCursor_keywordLike_필터로_제목_검색이_된다() {
    Content content1 = em.persist(Content.builder().type(ContentType.movie).externalId("ext-001").title("어벤져스").description("마블 영화").build());
    em.persist(Content.builder().type(ContentType.movie).externalId("ext-002").title("스파이더맨").description("마블 영화").build());
    em.flush();

    given(contentSearchService.searchIds("어벤져스"))
        .willReturn(List.of(content1.getId()));

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, "어벤져스", null, null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).title()).isEqualTo("어벤져스");
  }

  @Test
  @DisplayName("limit+1개를 조회하여 hasNext를 판단할 수 있다")
  void findContentsWithCursor_limit초과시_hasNext가_true다() {
    for (int i = 0; i < 5; i++) {
      em.persist(Content.builder()
          .type(ContentType.movie)
          .externalId("ext-00" + i)
          .title("영화" + i)
          .description("설명")
          .build());
    }
    em.flush();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, null, null, null, null, 3, "DESCENDING", "createdAt"
    );
    assertThat(result.data()).hasSize(3);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("tagsIn 필터로 태그가 포함된 콘텐츠만 조회한다")
  void findContentsWithCursor_tagsIn_필터로_태그_포함된_콘텐츠만_조회한다() {
    Content content1 = em.persist(Content.builder().type(ContentType.movie).externalId("ext-001").title("액션 영화").description("설명").build());
    Content content2 = em.persist(Content.builder().type(ContentType.movie).externalId("ext-002").title("로맨스 영화").description("설명").build());
    em.persist(ContentTag.builder().content(content1).tag("액션").build());
    em.persist(ContentTag.builder().content(content2).tag("로맨스").build());
    em.flush();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, null, List.of("액션"), null, null, 10, "DESCENDING", "createdAt"
    );

    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).title()).isEqualTo("액션 영화");
  }

  @Test
  @DisplayName("createdAt 커서로 다음 페이지를 조회한다")
  void findContentsWithCursor_createdAt_커서로_다음_페이지를_조회한다() {
    Content content1 = em.persist(Content.builder()
        .type(ContentType.movie).externalId("ext-001").title("영화1").description("설명").build());
    Content content2 = em.persist(Content.builder()
        .type(ContentType.movie).externalId("ext-002").title("영화2").description("설명").build());
    em.flush();

    LocalDateTime base = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    forceCreatedAt(content1, base.minusMinutes(1));
    forceCreatedAt(content2, base);

    // DB에 실제로 반영된 값을 다시 조회해서 커서로 사용
    Content refetchedContent1 = contentRepository.findById(content1.getId()).orElseThrow();
    String cursor = refetchedContent1.getCreatedAt().toString();
    UUID idAfter = refetchedContent1.getId();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, null, null, cursor, idAfter, 10, "ASCENDING", "createdAt"
    );

    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).title()).isEqualTo("영화2");
  }

  private void forceCreatedAt(Content content, LocalDateTime time) {
    em.getEntityManager().createQuery(
            "update Content c set c.createdAt = :t where c.id = :id")
        .setParameter("t", time)
        .setParameter("id", content.getId())
        .executeUpdate();
    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("watchedCount 커서로 다음 페이지를 조회한다")
  void findContentsWithCursor_watchedCount_커서로_다음_페이지를_조회한다() {
    Content content1 = em.persist(Content.builder()
        .type(ContentType.movie).externalId("ext-001").title("영화1").description("설명").build());
    Content content2 = em.persist(Content.builder()
        .type(ContentType.movie).externalId("ext-002").title("영화2").description("설명").build());

    content1.updateWatcherCount(100L);
    content2.updateWatcherCount(200L);
    em.flush();

    String cursor = "100";
    UUID idAfter = content1.getId();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, null, null, cursor, idAfter, 10, "ASCENDING", "watcherCount"
    );

    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).title()).isEqualTo("영화2");
  }

  @Test
  @DisplayName("rate 커서로 다음 페이지를 조회한다")
  void findContentsWithCursor_rate_커서로_다음_페이지를_조회한다() {
    Content content1 = em.persist(Content.builder()
        .type(ContentType.movie).externalId("ext-001").title("영화1").description("설명").build());
    Content content2 = em.persist(Content.builder()
        .type(ContentType.movie).externalId("ext-002").title("영화2").description("설명").build());

    content1.updateReviewStats(3.0, 1);
    content2.updateReviewStats(4.0, 1);
    em.flush();

    String cursor = "3.0";
    UUID idAfter = content1.getId();

    CursorResponseContentDto result = contentRepository.findContentsWithCursor(
        null, null, null, cursor, idAfter, 10, "ASCENDING", "rate"
    );

    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).title()).isEqualTo("영화2");
  }


}