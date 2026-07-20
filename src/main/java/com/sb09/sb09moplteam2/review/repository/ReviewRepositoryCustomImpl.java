package com.sb09.sb09moplteam2.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.review.entity.QReview;
import com.sb09.sb09moplteam2.review.entity.Review;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Review> findReviewsWithCursor(
      UUID contentId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  ) {
    QReview review = QReview.review;
    BooleanBuilder builder = new BooleanBuilder();

    if (contentId != null) {
      builder.and(review.content.id.eq(contentId));
    }

    boolean isAsc = "ASCENDING".equalsIgnoreCase(sortDirection);
    boolean sortByCreatedAt = "createdAt".equals(sortBy);

    if (cursor != null && !cursor.isBlank() && idAfter != null) {
      if (sortByCreatedAt) {
        Instant cursorValue = Instant.parse(cursor);
        builder.and(isAsc
            ? review.createdAt.gt(cursorValue)
            .or(review.createdAt.eq(cursorValue).and(review.id.gt(idAfter)))
            : review.createdAt.lt(cursorValue)
                .or(review.createdAt.eq(cursorValue).and(review.id.gt(idAfter)))
        );
      } else {
        Double cursorValue = Double.parseDouble(cursor);
        builder.and(isAsc
            ? review.rating.gt(cursorValue)
            .or(review.rating.eq(cursorValue).and(review.id.gt(idAfter)))
            : review.rating.lt(cursorValue)
                .or(review.rating.eq(cursorValue).and(review.id.gt(idAfter)))
        );
      }
    }

    OrderSpecifier<?> orderSpecifier = sortByCreatedAt
        ? (isAsc ? review.createdAt.asc() : review.createdAt.desc())
        : (isAsc ? review.rating.asc() : review.rating.desc());

    return queryFactory
        .selectFrom(review)
        .where(builder)
        .orderBy(orderSpecifier, review.id.asc())
        .limit(limit + 1)
        .fetch();
  }
}