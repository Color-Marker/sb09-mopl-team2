package com.sb09.sb09moplteam2.playlist.repository;

import com.sb09.sb09moplteam2.playlist.entity.PlaylistSubscription;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistSubscriptionRepository extends JpaRepository<PlaylistSubscription, UUID> {
  boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
  void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
  long countByPlaylistId(UUID playlistId);

  @Query("select ps.subscriber.id from PlaylistSubscription ps where ps.playlist.id = :playlistId")
  Set<UUID> findSubscriberIdsByPlaylistId(@Param("playlistId") UUID playlistId);
}
