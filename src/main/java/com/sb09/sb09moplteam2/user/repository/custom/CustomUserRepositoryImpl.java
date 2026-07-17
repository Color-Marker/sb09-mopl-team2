package com.sb09.sb09moplteam2.user.repository.custom;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.entity.QUser;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

  private static final QUser user = QUser.user;

  private final JPAQueryFactory queryFactory;

  @Override
  public List<User> searchUsers(UserSearchCondition condition) {
    return queryFactory
        .selectFrom(user)
        .where(
            emailLike(condition.getEmailLike()),
            roleEq(condition.getRoleEqual()),
            lockedEq(condition.getIsLocked()),
            cursorCondition(condition)
        )
        .orderBy(orderSpecifiers(condition))
        .limit(condition.getLimit() + 1L)
        .fetch();
  }

  @Override
  public long countUsers(UserSearchCondition condition) {
    Long count = queryFactory
        .select(user.count())
        .from(user)
        .where(
            emailLike(condition.getEmailLike()),
            roleEq(condition.getRoleEqual()),
            lockedEq(condition.getIsLocked())
        )
        .fetchOne();
    return count == null ? 0 : count;
  }

  private BooleanExpression emailLike(String emailLike) {
    return StringUtils.hasText(emailLike) ? user.email.containsIgnoreCase(emailLike) : null;
  }

  private BooleanExpression roleEq(Role role) {
    return role != null ? user.role.eq(role) : null;
  }

  private BooleanExpression lockedEq(Boolean isLocked) {
    return isLocked != null ? user.locked.eq(isLocked) : null;
  }

  private NumberExpression<Integer> lockedAsNumber() {
    return new CaseBuilder().when(user.locked.isTrue()).then(1).otherwise(0);
  }

  private boolean isAscending(UserSearchCondition condition) {
    return "ASCENDING".equalsIgnoreCase(condition.getSortDirection());
  }

  private String resolveSortBy(UserSearchCondition condition) {
    return condition.getSortBy() != null ? condition.getSortBy() : "createdAt";
  }

  private BooleanExpression cursorCondition(UserSearchCondition condition) {
    String cursor = condition.getCursor();
    if (!StringUtils.hasText(cursor)) {
      return null;
    }
    if (condition.getIdAfter() == null) {
      MoplException exception = new MoplException(ErrorCode.INVALID_REQUEST);
      exception.addDetail("idAfter", "커서 사용 시 보조 커서(idAfter)가 필요합니다");
      throw exception;
    }

    boolean asc = isAscending(condition);

    try {
      return buildCursorExpression(condition, cursor, asc);
    } catch (IllegalArgumentException | DateTimeParseException e) {
      MoplException exception = new MoplException(ErrorCode.INVALID_REQUEST, e);
      exception.addDetail("cursor", cursor);
      throw exception;
    }
  }

  private BooleanExpression buildCursorExpression(UserSearchCondition condition, String cursor, boolean asc) {
    return switch (resolveSortBy(condition)) {
      case "email" -> asc
          ? user.email.gt(cursor).or(user.email.eq(cursor).and(user.id.gt(condition.getIdAfter())))
          : user.email.lt(cursor).or(user.email.eq(cursor).and(user.id.lt(condition.getIdAfter())));
      case "name" -> asc
          ? user.name.gt(cursor).or(user.name.eq(cursor).and(user.id.gt(condition.getIdAfter())))
          : user.name.lt(cursor).or(user.name.eq(cursor).and(user.id.lt(condition.getIdAfter())));
      case "role" -> {
        Role roleCursor = Role.valueOf(cursor);
        yield asc
            ? user.role.gt(roleCursor).or(user.role.eq(roleCursor).and(user.id.gt(condition.getIdAfter())))
            : user.role.lt(roleCursor).or(user.role.eq(roleCursor).and(user.id.lt(condition.getIdAfter())));
      }
      case "isLocked" -> {
        int lockedCursor = Boolean.parseBoolean(cursor) ? 1 : 0;
        yield asc
            ? lockedAsNumber().gt(lockedCursor).or(lockedAsNumber().eq(lockedCursor).and(user.id.gt(condition.getIdAfter())))
            : lockedAsNumber().lt(lockedCursor).or(lockedAsNumber().eq(lockedCursor).and(user.id.lt(condition.getIdAfter())));
      }
      default -> { // createdAt
        Instant createdAtCursor = Instant.parse(cursor);
        yield asc
            ? user.createdAt.gt(createdAtCursor).or(user.createdAt.eq(createdAtCursor).and(user.id.gt(condition.getIdAfter())))
            : user.createdAt.lt(createdAtCursor).or(user.createdAt.eq(createdAtCursor).and(user.id.lt(condition.getIdAfter())));
      }
    };
  }

  private OrderSpecifier<?>[] orderSpecifiers(UserSearchCondition condition) {
    Order order = isAscending(condition) ? Order.ASC : Order.DESC;

    OrderSpecifier<?> primary = switch (resolveSortBy(condition)) {
      case "email" -> new OrderSpecifier<>(order, user.email);
      case "name" -> new OrderSpecifier<>(order, user.name);
      case "role" -> new OrderSpecifier<>(order, user.role);
      case "isLocked" -> new OrderSpecifier<>(order, lockedAsNumber());
      default -> new OrderSpecifier<>(order, user.createdAt);
    };

    OrderSpecifier<?> tieBreak = new OrderSpecifier<>(order, user.id);

    return new OrderSpecifier<?>[]{primary, tieBreak};
  }
}