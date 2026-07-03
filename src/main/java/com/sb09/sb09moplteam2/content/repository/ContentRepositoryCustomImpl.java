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
import com.sb09.sb09moplteam2.dto.ContentSummary;
import java.time.LocalDateTime;
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

    BooleanBuilder builder = new BooleanBuilder();

    // 필터 조건
    if (typeEqual != null) {
      builder.and(content.type.eq(ContentType.valueOf(typeEqual)));
    }
    if (keywordLike != null) {
      builder.and(content.title.containsIgnoreCase(keywordLike)
          .or(content.description.containsIgnoreCase(keywordLike)));
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
      boolean isAsc = "ASCENDING".equals(sortDirection);
      switch (sortBy) {
        case "createdAt" -> {
          LocalDateTime cursorValue = LocalDateTime.parse(cursor);
          builder.and(isAsc
              ? content.createdAt.gt(cursorValue)
              .or(content.createdAt.eq(cursorValue).and(content.id.gt(idAfter)))
              : content.createdAt.lt(cursorValue)
                  .or(content.createdAt.eq(cursorValue).and(content.id.lt(idAfter)))
          );
        }
        case "watchedCount" -> {
          Long cursorValue = Long.parseLong(cursor);
          builder.and(isAsc
              ? content.watcherCount.gt(cursorValue)
              .or(content.watcherCount.eq(cursorValue).and(content.id.gt(idAfter)))
              : content.watcherCount.lt(cursorValue)
                  .or(content.watcherCount.eq(cursorValue).and(content.id.lt(idAfter)))
          );
        }
        case "rate" -> {
          Double cursorValue = Double.parseDouble(cursor);
          builder.and(isAsc
              ? content.averageRating.gt(cursorValue)
              .or(content.averageRating.eq(cursorValue).and(content.id.gt(idAfter)))
              : content.averageRating.lt(cursorValue)
                  .or(content.averageRating.eq(cursorValue).and(content.id.lt(idAfter)))
          );
        }
      }
    }

    // 정렬
    boolean isAsc = "ASCENDING".equals(sortDirection);
    OrderSpecifier<?> orderSpecifier = switch (sortBy) {
      case "watchedCount" -> isAsc ? content.watcherCount.asc() : content.watcherCount.desc();
      case "rate" -> isAsc ? content.averageRating.asc() : content.averageRating.desc();
      default -> isAsc ? content.createdAt.asc() : content.createdAt.desc();
    };

    // limit + 1 조회 (hasNext 판단용)
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

    // 태그 조회
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

    // ContentSummary 변환
    List<ContentSummary> data = contents.stream()
        .map(c -> new ContentSummary(
            c.getId(),
            c.getType(),
            c.getTitle(),
            c.getDescription(),
            c.getThumbnailUrl(),
            tagMap.getOrDefault(c.getId(), List.of()),
            c.getAverageRating(),
            c.getReviewCount()
        ))
        .toList();

    // 총 개수
    Long totalCount = queryFactory
        .select(content.count())
        .from(content)
        .where(builder)
        .fetchOne();

    // nextCursor, nextIdAfter
    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext) {
      Content last = contents.get(contents.size() - 1);
      nextCursor = switch (sortBy) {
        case "watchedCount" -> String.valueOf(last.getWatcherCount());
        case "rate" -> String.valueOf(last.getAverageRating());
        default -> last.getCreatedAt().toString();
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