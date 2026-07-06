package com.sb09.sb09moplteam2.notification.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.common.SortDirection;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import com.sb09.sb09moplteam2.notification.entity.QNotification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QNotification notification = QNotification.notification;

  @Override
  public Slice<Notification> searchNotification(UUID receiverId, NotificationListRequest request) {
    boolean isDesc = request.getSortDirection() == SortDirection.DESCENDING;

    List<Notification> results = queryFactory
        .selectFrom(notification)
        .where(
            notification.receiver.id.eq(receiverId),
            cursorCondition(request)
        )
        .orderBy(
            isDesc ? notification.createdAt.desc() : notification.createdAt.asc(),
            isDesc ? notification.id.desc() : notification.id.asc()
        )
        .limit(request.getLimit() + 1)
        .fetch();

    boolean hasNext = results.size() > request.getLimit();
    if(hasNext){
      results.remove(request.getLimit());
    }

    return new SliceImpl<>(
        results,
        PageRequest.of(0, request.getLimit()),
        hasNext
    );
  }


  private BooleanExpression cursorCondition(NotificationListRequest request){
    if(request.getCursor() == null || request.getIdAfter() == null){
      // 첫 페이지임.
      return null;
    }

    Instant cursor = Instant.parse(request.getCursor());
    UUID idAfter = request.getIdAfter();

    if(request.getSortDirection() == SortDirection.DESCENDING){
      return notification.createdAt.lt(cursor)
          .or(notification.createdAt.eq(cursor).and(notification.id.lt(idAfter)));
    }
    else{
      return notification.createdAt.gt(cursor)
          .or(notification.createdAt.eq(cursor).and(notification.id.gt(idAfter)));
    }
  }
}
