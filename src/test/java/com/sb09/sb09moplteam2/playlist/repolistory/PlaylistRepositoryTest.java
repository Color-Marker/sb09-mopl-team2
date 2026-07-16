package com.sb09.sb09moplteam2.playlist.repolistory;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistSubscription;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistSubscriptionRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class, TestJpaConfig.class})
class PlaylistRepositoryTest {

  @Autowired
  private PlaylistRepository playlistRepository;

  @Autowired
  private PlaylistSubscriptionRepository playlistSubscriptionRepository;

  @Autowired
  private EntityManager entityManager;

  private User saveUser(String name, String email) {
    User user = new User(name, email, "password");
    entityManager.persist(user);
    return user;
  }

  private Playlist savePlaylist(String title, String description, User owner) {
    Playlist playlist = Playlist.builder()
        .title(title)
        .description(description)
        .owner(owner)
        .build();
    entityManager.persist(playlist);
    return playlist;
  }

  private void forceUpdatedAt(Playlist playlist, Instant instant) {
    entityManager.flush();
    entityManager.createQuery("update Playlist p set p.updatedAt = :t where p.id = :id")
        .setParameter("t", instant)
        .setParameter("id", playlist.getId())
        .executeUpdate();
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void 제목_키워드로_검색된다() {
    User owner = saveUser("우디", "woody@mopl.io");
    savePlaylist("액션 영화 모음", "설명", owner);
    savePlaylist("코미디 모음", "설명", owner);
    entityManager.flush();
    entityManager.clear();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        "액션", null, null, null, null, 10, "DESCENDING", "updatedAt");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("액션 영화 모음");
  }

  @Test
  void ownerId로_필터링된다() {

    User owner1 = saveUser("우디", "woody@mopl.io");
    User owner2 = saveUser("버즈", "buzz@mopl.io");
    savePlaylist("우디꺼", "설명", owner1);
    savePlaylist("버즈꺼", "설명", owner2);
    entityManager.flush();
    entityManager.clear();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, owner1.getId(), null, null, null, 10, "DESCENDING", "updatedAt");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("우디꺼");
  }

  @Test
  void subscriberId로_필터링된다() {
    User owner = saveUser("우디", "woody@mopl.io");
    User subscriber = saveUser("버즈", "buzz@mopl.io");
    Playlist subscribed = savePlaylist("구독한플레이리스트", "설명", owner);
    savePlaylist("구독안한플레이리스트", "설명", owner);

    entityManager.persist(PlaylistSubscription.builder()
        .playlist(subscribed)
        .subscriber(subscriber)
        .build());
    entityManager.flush();
    entityManager.clear();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, subscriber.getId(), null, null, 10, "DESCENDING", "updatedAt");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("구독한플레이리스트");
  }

  @Test
  void updatedAt_기준_내림차순으로_정렬된다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("첫번째", "설명", owner);
    Playlist p2 = savePlaylist("두번째", "설명", owner);
    Playlist p3 = savePlaylist("세번째", "설명", owner);

    Instant now = Instant.now();
    forceUpdatedAt(p1, now.minus(2, ChronoUnit.MINUTES));
    forceUpdatedAt(p2, now.minus(1, ChronoUnit.MINUTES));
    forceUpdatedAt(p3, now);

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null, null, null, 10, "DESCENDING", "updatedAt");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("세번째", "두번째", "첫번째");
  }

  @Test
  void updatedAt_기준_오름차순으로_정렬된다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("첫번째", "설명", owner);
    Playlist p2 = savePlaylist("두번째", "설명", owner);
    Playlist p3 = savePlaylist("세번째", "설명", owner);

    Instant now = Instant.now();
    forceUpdatedAt(p1, now.minus(2, ChronoUnit.MINUTES));
    forceUpdatedAt(p2, now.minus(1, ChronoUnit.MINUTES));
    forceUpdatedAt(p3, now);

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null, null, null, 10, "ASCENDING", "updatedAt");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("첫번째", "두번째", "세번째");
  }

  @Test
  void subscriberCount_기준_내림차순으로_정렬된다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("낮은구독", "설명", owner);
    Playlist p2 = savePlaylist("높은구독", "설명", owner);
    p1.incrementSubscriberCount();
    p2.incrementSubscriberCount();
    p2.incrementSubscriberCount();
    p2.incrementSubscriberCount();
    entityManager.flush();
    entityManager.clear();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null, null, null, 10, "DESCENDING", "subscriberCount");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("높은구독", "낮은구독");
  }

  @Test
  void updatedAt_커서_기준으로_다음_페이지를_조회한다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("첫번째", "설명", owner);
    Playlist p2 = savePlaylist("두번째", "설명", owner);
    Playlist p3 = savePlaylist("세번째", "설명", owner);

    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    forceUpdatedAt(p1, now.minus(3, ChronoUnit.MINUTES));
    forceUpdatedAt(p2, now.minus(2, ChronoUnit.MINUTES));
    forceUpdatedAt(p3, now.minus(1, ChronoUnit.MINUTES));

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null,
        now.minus(1, ChronoUnit.MINUTES).toString(), p3.getId(),
        10, "DESCENDING", "updatedAt");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("두번째", "첫번째");
  }

  @Test
  void subscriberCount_커서_기준으로_다음_페이지를_조회한다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("첫번째", "설명", owner);
    Playlist p2 = savePlaylist("두번째", "설명", owner);
    Playlist p3 = savePlaylist("세번째", "설명", owner);

    p1.incrementSubscriberCount();
    p2.incrementSubscriberCount();
    p2.incrementSubscriberCount();
    p3.incrementSubscriberCount();
    p3.incrementSubscriberCount();
    p3.incrementSubscriberCount();
    entityManager.flush();
    entityManager.clear();

    p3 = playlistRepository.findById(p3.getId()).orElseThrow();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null, "3", p3.getId(), 10, "DESCENDING", "subscriberCount");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("두번째", "첫번째");
  }

  @Test
  void limit보다_1개_많이_조회하여_hasNext_판단용_데이터를_제공한다() {
    User owner = saveUser("우디", "woody@mopl.io");
    savePlaylist("첫번째", "설명", owner);
    savePlaylist("두번째", "설명", owner);
    savePlaylist("세번째", "설명", owner);
    entityManager.flush();
    entityManager.clear();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null, null, null, 2, "DESCENDING", "updatedAt");

    assertThat(result).hasSize(3);
  }

  @Test
  void updatedAt_커서_기준으로_다음_페이지를_오름차순으로_조회한다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("첫번째", "설명", owner);
    Playlist p2 = savePlaylist("두번째", "설명", owner);
    Playlist p3 = savePlaylist("세번째", "설명", owner);

    Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
    forceUpdatedAt(p1, now.minus(3, ChronoUnit.MINUTES));
    forceUpdatedAt(p2, now.minus(2, ChronoUnit.MINUTES));
    forceUpdatedAt(p3, now.minus(1, ChronoUnit.MINUTES));

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null,
        now.minus(3, ChronoUnit.MINUTES).toString(), p1.getId(),
        10, "ASCENDING", "updatedAt");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("두번째", "세번째");
  }

  @Test
  void subscriberCount_커서_기준으로_다음_페이지를_오름차순으로_조회한다() {
    User owner = saveUser("우디", "woody@mopl.io");
    Playlist p1 = savePlaylist("첫번째", "설명", owner);
    Playlist p2 = savePlaylist("두번째", "설명", owner);
    Playlist p3 = savePlaylist("세번째", "설명", owner);

    p2.incrementSubscriberCount();
    p3.incrementSubscriberCount();
    p3.incrementSubscriberCount();
    entityManager.flush();
    entityManager.clear();

    p1 = playlistRepository.findById(p1.getId()).orElseThrow();

    List<Playlist> result = playlistRepository.findPlaylistsWithCursor(
        null, null, null, "0", p1.getId(), 10, "ASCENDING", "subscriberCount");

    assertThat(result).extracting(Playlist::getTitle)
        .containsExactly("두번째", "세번째");
  }
}