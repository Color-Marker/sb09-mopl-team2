package com.sb09.sb09moplteam2.playlist.repository;

import com.sb09.sb09moplteam2.playlist.entity.PlaylistSubscription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSubscriptionRepository extends JpaRepository<PlaylistSubscription, UUID> {
  boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
  void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
  long countByPlaylistId(UUID playlistId);
}
