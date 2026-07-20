package com.sb09.sb09moplteam2.playlist.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.QPlaylist;
import com.sb09.sb09moplteam2.playlist.entity.QPlaylistSubscription;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaylistRepositoryCustomImpl implements PlaylistRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Playlist> findPlaylistsWithCursor(
      String keywordLike,
      UUID ownerIdEqual,
      UUID subscriberIdEqual,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  ) {
    QPlaylist playlist = QPlaylist.playlist;
    QPlaylistSubscription subscription = QPlaylistSubscription.playlistSubscription;

    BooleanBuilder builder = new BooleanBuilder();

    if (keywordLike != null && !keywordLike.isBlank()) {
      builder.and(playlist.title.containsIgnoreCase(keywordLike.trim()));
    }
    if (ownerIdEqual != null) {
      builder.and(playlist.owner.id.eq(ownerIdEqual));
    }
    if (subscriberIdEqual != null) {
      builder.and(playlist.id.in(
          JPAExpressions.select(subscription.playlist.id)
              .from(subscription)
              .where(subscription.subscriber.id.eq(subscriberIdEqual))
      ));
    }

    boolean isAsc = "ASCENDING".equalsIgnoreCase(sortDirection);
    boolean sortByUpdatedAt = "updatedAt".equals(sortBy);

    if (cursor != null && !cursor.isBlank() && idAfter != null) {
      if (sortByUpdatedAt) {
        Instant cursorValue = Instant.parse(cursor);
        builder.and(isAsc
            ? playlist.updatedAt.gt(cursorValue)
            .or(playlist.updatedAt.eq(cursorValue).and(playlist.id.gt(idAfter)))
            : playlist.updatedAt.lt(cursorValue)
                .or(playlist.updatedAt.eq(cursorValue).and(playlist.id.gt(idAfter)))
        );
      } else {
        Long cursorValue = Long.parseLong(cursor);
        builder.and(isAsc
            ? playlist.subscriberCount.gt(cursorValue)
            .or(playlist.subscriberCount.eq(cursorValue).and(playlist.id.gt(idAfter)))
            : playlist.subscriberCount.lt(cursorValue)
                .or(playlist.subscriberCount.eq(cursorValue).and(playlist.id.gt(idAfter)))
        );
      }
    }

    OrderSpecifier<?> orderSpecifier = sortByUpdatedAt
        ? (isAsc ? playlist.updatedAt.asc() : playlist.updatedAt.desc())
        : (isAsc ? playlist.subscriberCount.asc() : playlist.subscriberCount.desc());

    return queryFactory
        .selectFrom(playlist)
        .where(builder)
        .orderBy(orderSpecifier, playlist.id.asc())
        .limit(limit + 1)
        .fetch();
  }
}