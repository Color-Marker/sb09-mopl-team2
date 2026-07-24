package com.sb09.sb09moplteam2.content.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.entity.QContent;
import com.sb09.sb09moplteam2.content.entity.QContentTag;
import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryCustomImpl implements ContentRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final ContentSearchService contentSearchService;

  @Override
  public CursorResponseContentDto findContentsWithCursor(
      String typeEqual,
      String keywordLike,
      List<String> tagsIn,
      String cursor,
      UUID idAfter,
      Integer limit,
      String sortDirection,
      String sortBy
  ) {
    QContent content = QContent.content;
    QContentTag contentTag = QContentTag.contentTag;

    boolean isAsc = "ASCENDING".equalsIgnoreCase(sortDirection);

    BooleanBuilder builder = new BooleanBuilder();

    if (typeEqual != null) {
      builder.and(content.type.eq(ContentType.valueOf(typeEqual)));
    }
    if (keywordLike != null && !keywordLike.isBlank()) {
      List<UUID> matchedIds = contentSearchService.searchIds(keywordLike.trim());
      if (matchedIds.isEmpty()) {
        return new CursorResponseContentDto(List.of(), null, null, false, 0L, sortBy, sortDirection);
      }
      builder.and(content.id.in(matchedIds));
    }
    if (tagsIn != null && !tagsIn.isEmpty()) {
      builder.and(content.id.in(
          JPAExpressions.select(contentTag.content.id)
              .from(contentTag)
              .where(contentTag.tag.in(tagsIn))
      ));
    }

    // 커서 조건
    if (cursor != null && idAfter != null) {
      switch (sortBy) {
        case "watcherCount" -> {
          Long cursorValue = Long.parseLong(cursor);
          builder.and(isAsc
              ? content.watcherCount.gt(cursorValue)
              .or(content.watcherCount.eq(cursorValue).and(content.id.gt(idAfter)))
              : content.watcherCount.lt(cursorValue)
                  .or(content.watcherCount.eq(cursorValue).and(content.id.gt(idAfter)))
          );
        }
        case "rate" -> {
          Double cursorValue = Double.parseDouble(cursor);
          builder.and(isAsc
              ? content.averageRating.gt(cursorValue)
              .or(content.averageRating.eq(cursorValue).and(content.id.gt(idAfter)))
              : content.averageRating.lt(cursorValue)
                  .or(content.averageRating.eq(cursorValue).and(content.id.gt(idAfter)))
          );
        }
        default -> {
          LocalDate cursorValue = LocalDate.parse(cursor);
          builder.and(isAsc
              ? content.releaseDate.gt(cursorValue)
              .or(content.releaseDate.eq(cursorValue).and(content.id.gt(idAfter)))
              : content.releaseDate.lt(cursorValue)
                  .or(content.releaseDate.eq(cursorValue).and(content.id.gt(idAfter)))
          );
        }
      }
    }

    // 정렬
    OrderSpecifier<?> orderSpecifier = switch (sortBy) {
      case "watcherCount" -> isAsc ? content.watcherCount.asc() : content.watcherCount.desc();
      case "rate" -> isAsc ? content.averageRating.asc() : content.averageRating.desc();
      default -> isAsc ? content.releaseDate.asc().nullsLast() : content.releaseDate.desc().nullsLast();
    };

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(builder)
        .orderBy(orderSpecifier, content.id.asc())
        .limit(limit + 1)
        .fetch();

    boolean hasNext = contents.size() > limit;
    if (hasNext) {
      contents = contents.subList(0, limit);
    }

    List<UUID> contentIds = contents.stream().map(Content::getId).toList();
    List<ContentTag> tags = queryFactory
        .selectFrom(contentTag)
        .where(contentTag.content.id.in(contentIds))
        .fetch();

    Map<UUID, List<String>> tagMap = tags.stream()
        .collect(Collectors.groupingBy(
            t -> t.getContent().getId(),
            Collectors.mapping(ContentTag::getTag, Collectors.toList())
        ));

    List<ContentSummary> data = contents.stream()
        .map(c -> new ContentSummary(
            c.getId(),
            c.getType(),
            c.getTitle(),
            c.getDescription(),
            c.getThumbnailUrl(),
            tagMap.getOrDefault(c.getId(), List.of()),
            c.getAverageRating(),
            c.getReviewCount(),
            c.getWatcherCount()
        ))
        .toList();

    Long totalCount = null;
    if (idAfter == null) {
      totalCount = queryFactory
          .select(content.count())
          .from(content)
          .where(builder)
          .fetchOne();
    }

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext) {
      Content last = contents.get(contents.size() - 1);
      nextCursor = switch (sortBy) {
        case "watcherCount" -> String.valueOf(last.getWatcherCount());
        case "rate" -> String.valueOf(last.getAverageRating());
        default -> last.getReleaseDate() != null ? last.getReleaseDate().toString() : null;
      };
      nextIdAfter = last.getId();
    }

    return new CursorResponseContentDto(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }
}