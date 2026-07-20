package com.sb09.sb09moplteam2.playlist.repository;

import com.sb09.sb09moplteam2.playlist.entity.PlaylistItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, UUID> {
  List<PlaylistItem> findByPlaylistIdOrderByOrderIndex(UUID playlistId);
  List<PlaylistItem> findByPlaylistIdInOrderByOrderIndex(List<UUID> playlistIds);
  boolean existsByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
  void deleteByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
}
