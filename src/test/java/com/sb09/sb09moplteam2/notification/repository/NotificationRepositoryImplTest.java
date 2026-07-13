package com.sb09.sb09moplteam2.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.common.SortDirection;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import com.sb09.sb09moplteam2.notification.entity.NotificationLevel;
import com.sb09.sb09moplteam2.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(NotificationRepositoryImplTest.QuerydslTestConfig.class)
class NotificationRepositoryImplTest {

  @TestConfiguration
  @EnableJpaAuditing
  static class QuerydslTestConfig {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(entityManager);
    }
  }

  @Autowired
  private TestEntityManager em;

  @Autowired
  private JPAQueryFactory queryFactory;

  private NotificationRepositoryImpl notificationRepository;

  private User receiver;
  private User otherReceiver;

  @BeforeEach
  void setUp() {
    notificationRepository = new NotificationRepositoryImpl(queryFactory);

    receiver = em.persist(new User("receiver", "receiver@test.com", "password"));
    otherReceiver = em.persist(new User("other", "other@test.com", "password"));
  }

  private Notification createAndPersist(User receiver, Instant createdAt) {
    Notification notification = new Notification(
        receiver, "title", "content", NotificationLevel.INFO);
    em.persist(notification);
    em.flush();

    em.getEntityManager()
        .createNativeQuery("update notifications set created_at = ?1 where id = ?2")
        .setParameter(1, java.sql.Timestamp.from(createdAt))
        .setParameter(2, notification.getId())
        .executeUpdate();

    em.getEntityManager().refresh(notification);

    return notification;
  }

  private NotificationListRequest createRequest(int limit, SortDirection direction) {
    return createRequest(limit, direction, null, null);
  }

  private NotificationListRequest createRequest(
      int limit, SortDirection direction, String cursor, UUID idAfter) {
    NotificationListRequest request = new NotificationListRequest();
    request.setLimit(limit);
    request.setSortDirection(direction);
    request.setSortBy("createdAt");
    request.setCursor(cursor);
    request.setIdAfter(idAfter);
    return request;
  }

  @Test
  void searchNotification_첫페이지_DESC_정렬_최신순() {
    Instant base = Instant.now();
    Notification n1 = createAndPersist(receiver, base.minus(3, ChronoUnit.MINUTES));
    Notification n2 = createAndPersist(receiver, base.minus(2, ChronoUnit.MINUTES));
    Notification n3 = createAndPersist(receiver, base.minus(1, ChronoUnit.MINUTES));
    em.clear();

    NotificationListRequest request = createRequest(10, SortDirection.DESCENDING);

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    assertThat(result.getContent())
        .extracting(Notification::getId)
        .containsExactly(n3.getId(), n2.getId(), n1.getId());
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void searchNotification_첫페이지_ASC_정렬_오래된순() {
    Instant base = Instant.now();
    Notification n1 = createAndPersist(receiver, base.minus(3, ChronoUnit.MINUTES));
    Notification n2 = createAndPersist(receiver, base.minus(2, ChronoUnit.MINUTES));
    Notification n3 = createAndPersist(receiver, base.minus(1, ChronoUnit.MINUTES));
    em.clear();

    NotificationListRequest request = createRequest(10, SortDirection.ASCENDING);

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    assertThat(result.getContent())
        .extracting(Notification::getId)
        .containsExactly(n1.getId(), n2.getId(), n3.getId());
  }

  @Test
  void searchNotification_limit_초과시_hasNext_true_및_초과분_제거() {
    Instant base = Instant.now();
    for (int i = 0; i < 5; i++) {
      createAndPersist(receiver, base.minus(5 - i, ChronoUnit.MINUTES));
    }
    em.clear();

    NotificationListRequest request = createRequest(3, SortDirection.DESCENDING);

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    assertThat(result.getContent()).hasSize(3);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  void searchNotification_커서_기준_이후_DESC_다음페이지_조회() {
    Instant base = Instant.now();
    Notification n1 = createAndPersist(receiver, base.minus(4, ChronoUnit.MINUTES));
    Notification n2 = createAndPersist(receiver, base.minus(3, ChronoUnit.MINUTES));
    Notification n3 = createAndPersist(receiver, base.minus(2, ChronoUnit.MINUTES));
    Notification n4 = createAndPersist(receiver, base.minus(1, ChronoUnit.MINUTES));
    em.clear();

    Notification cursorTarget = em.find(Notification.class, n3.getId());

    NotificationListRequest request = createRequest(
        10, SortDirection.DESCENDING,
        cursorTarget.getCreatedAt().toString(), cursorTarget.getId());

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    assertThat(result.getContent())
        .extracting(Notification::getId)
        .containsExactly(n2.getId(), n1.getId());
  }

  @Test
  void searchNotification_커서_기준_이후_ASC_다음페이지_조회() {
    Instant base = Instant.now();
    Notification n1 = createAndPersist(receiver, base.minus(4, ChronoUnit.MINUTES));
    Notification n2 = createAndPersist(receiver, base.minus(3, ChronoUnit.MINUTES));
    Notification n3 = createAndPersist(receiver, base.minus(2, ChronoUnit.MINUTES));
    Notification n4 = createAndPersist(receiver, base.minus(1, ChronoUnit.MINUTES));
    em.clear();

    Notification cursorTarget = em.find(Notification.class, n2.getId());

    NotificationListRequest request = createRequest(
        10, SortDirection.ASCENDING,
        cursorTarget.getCreatedAt().toString(), cursorTarget.getId());

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    assertThat(result.getContent())
        .extracting(Notification::getId)
        .containsExactly(n3.getId(), n4.getId());
  }

  @Test
  void searchNotification_다른_receiver의_알림은_조회되지_않음() {
    Instant now = Instant.now();
    Notification mine = createAndPersist(receiver, now);
    createAndPersist(otherReceiver, now);

    em.clear();

    NotificationListRequest request = createRequest(10, SortDirection.DESCENDING);

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    assertThat(result.getContent())
        .extracting(Notification::getId)
        .containsExactly(mine.getId());
  }

  @Test
  void searchNotification_createdAt_같으면_id로_tie_break() {
    Instant same = Instant.now();
    Notification n1 = createAndPersist(receiver, same);
    Notification n2 = createAndPersist(receiver, same);
    em.clear();

    NotificationListRequest request = createRequest(10, SortDirection.DESCENDING);

    Slice<Notification> result = notificationRepository.searchNotification(
        receiver.getId(), request);

    List<UUID> ids = result.getContent().stream().map(Notification::getId).toList();

    List<UUID> expected = List.of(n1.getId(), n2.getId()).stream()
        .sorted((a, b) -> b.toString().compareTo(a.toString()))
        .toList();

    assertThat(ids).isEqualTo(expected);
  }
}