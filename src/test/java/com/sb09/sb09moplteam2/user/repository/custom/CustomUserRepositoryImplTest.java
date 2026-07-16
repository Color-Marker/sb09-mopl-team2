package com.sb09.sb09moplteam2.user.repository.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.config.JpaAuditingConfig;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({CustomUserRepositoryImplTest.QueryDslConfig.class, JpaAuditingConfig.class})
class CustomUserRepositoryImplTest {

  static class QueryDslConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
      return new JPAQueryFactory(em);
    }
  }

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager em;

  private User userA;
  private User userB;
  private User userC;

  @BeforeEach
  void setUp() {
    userA = new User("앨리스", "alice@mopl.io", "pw");
    userB = new User("밥", "bob@mopl.io", "pw");
    userC = new User("찰리", "charlie@mopl.io", "pw");

    em.persist(userA);
    em.persist(userB);
    em.persist(userC);
    em.flush();
    em.clear();
  }

  @Test
  void searchUsers_조건없이_전체조회() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).hasSize(3);
  }

  @Test
  void searchUsers_emailLike_필터() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .emailLike("bob")
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEmail()).isEqualTo("bob@mopl.io");
  }

  @Test
  void searchUsers_roleEqual_필터() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .roleEqual(Role.USER)
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).hasSize(3);
  }

  @Test
  void searchUsers_isLocked_필터() {
    userA.changeLocked(true);
    userRepository.save(userA);
    em.flush();
    em.clear();

    UserSearchCondition condition = UserSearchCondition.builder()
        .isLocked(true)
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).isLocked()).isTrue();
  }

  @Test
  void searchUsers_limit보다_많으면_limit개만_반환() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(2)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    // searchUsers는 limit+1 fetch (hasNext 판단은 서비스에서)
    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).hasSize(3); // limit+1 = 3
  }

  @Test
  void searchUsers_name_커서_페이지네이션_ASCENDING() {
    // 이름 오름차순으로 앨리스 → 밥 → 찰리 순서
    // 커서: "밥" 이후 조회
    User first = userRepository.searchUsers(UserSearchCondition.builder()
        .limit(1).sortBy("name").sortDirection("ASCENDING").build()).get(0);

    UserSearchCondition nextPage = UserSearchCondition.builder()
        .cursor(first.getName())
        .idAfter(first.getId())
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(nextPage);

    assertThat(result).isNotEmpty();
    result.forEach(u -> assertThat(u.getName()).isGreaterThanOrEqualTo(first.getName()));
  }

  @Test
  void searchUsers_email_커서_DESCENDING() {
    List<User> first = userRepository.searchUsers(UserSearchCondition.builder()
        .limit(1).sortBy("email").sortDirection("DESCENDING").build());

    User last = first.get(0);

    UserSearchCondition nextPage = UserSearchCondition.builder()
        .cursor(last.getEmail())
        .idAfter(last.getId())
        .limit(10)
        .sortBy("email")
        .sortDirection("DESCENDING")
        .build();

    List<User> result = userRepository.searchUsers(nextPage);

    assertThat(result).isNotEmpty();
  }

  @Test
  void searchUsers_role_커서() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .cursor("USER")
        .idAfter(userA.getId())
        .limit(10)
        .sortBy("role")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).isNotNull();
  }

  @Test
  void searchUsers_isLocked_커서() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .cursor("false")
        .idAfter(userA.getId())
        .limit(10)
        .sortBy("isLocked")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).isNotNull();
  }

  @Test
  void searchUsers_createdAt_커서() {
    List<User> first = userRepository.searchUsers(UserSearchCondition.builder()
        .limit(1).sortBy("createdAt").sortDirection("ASCENDING").build());

    User pivot = first.get(0);

    UserSearchCondition nextPage = UserSearchCondition.builder()
        .cursor(pivot.getCreatedAt().toString())
        .idAfter(pivot.getId())
        .limit(10)
        .sortBy("createdAt")
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(nextPage);

    assertThat(result).isNotNull();
  }

  @Test
  void searchUsers_sortBy_null이면_createdAt으로_정렬() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(10)
        .sortDirection("ASCENDING")
        .build();

    List<User> result = userRepository.searchUsers(condition);

    assertThat(result).hasSize(3);
  }

  @Test
  void countUsers_전체_카운트() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    long count = userRepository.countUsers(condition);

    assertThat(count).isEqualTo(3);
  }

  @Test
  void countUsers_emailLike_필터_카운트() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .emailLike("alice")
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    long count = userRepository.countUsers(condition);

    assertThat(count).isEqualTo(1);
  }

  @Test
  void countUsers_결과없으면_0반환() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .emailLike("nonexistent")
        .limit(10)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    long count = userRepository.countUsers(condition);

    assertThat(count).isZero();
  }

  @Test
  void searchUsers_잘못된_형식의_cursor면_MoplException을_던진다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .cursor("not-an-instant")
        .idAfter(UUID.randomUUID())
        .limit(10)
        .sortBy("createdAt")
        .sortDirection("DESCENDING")
        .build();

    assertThatThrownBy(() -> userRepository.searchUsers(condition))
        .isInstanceOf(MoplException.class);
  }

  @Test
  void searchUsers_잘못된_role_cursor면_MoplException을_던진다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .cursor("NOT_A_ROLE")
        .idAfter(UUID.randomUUID())
        .limit(10)
        .sortBy("role")
        .sortDirection("ASCENDING")
        .build();

    assertThatThrownBy(() -> userRepository.searchUsers(condition))
        .isInstanceOf(MoplException.class);
  }

  @Test
  void searchUsers_cursor만_있고_idAfter가_없으면_MoplException을_던진다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .cursor("alice@mopl.io")
        .limit(10)
        .sortBy("email")
        .sortDirection("ASCENDING")
        .build();

    assertThatThrownBy(() -> userRepository.searchUsers(condition))
        .isInstanceOf(MoplException.class);
  }
}