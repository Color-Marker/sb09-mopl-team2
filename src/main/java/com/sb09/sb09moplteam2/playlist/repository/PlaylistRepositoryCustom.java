package com.sb09.sb09moplteam2.playlist.repository;

import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import java.util.List;
import java.util.UUID;

public interface PlaylistRepositoryCustom {
  List<Playlist> findPlaylistsWithCursor(
      String keywordLike,
      UUID ownerIdEqual,
      UUID subscriberIdEqual,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  );

  Long countPlaylists(
      String keywordLike,
      UUID ownerIdEqual,
      UUID subscriberIdEqual
  );
}
