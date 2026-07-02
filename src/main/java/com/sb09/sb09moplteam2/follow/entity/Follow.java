package com.sb09.sb09moplteam2.follow.entity;

import com.sb09.sb09moplteam2.exception.follow.SelfFollowNotAllowedException;
import com.sb09.sb09moplteam2.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "follows",
    uniqueConstraints = {
        // 중복 팔로우 방지: DB 레벨에서 확실하게 무결성을 보장합니다.
        @UniqueConstraint(
            name = "uk_follower_followee",
            columnNames = {"follower_id", "followee_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "followee_id", nullable = false)
  private User followee;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public Follow(User follower, User followee) {
    validateSelfFollow(follower, followee);
    this.follower = follower;
    this.followee = followee;
  }

  private void validateSelfFollow(User follower, User followee) {
    if (follower.getId().equals(followee.getId())) {
      throw new SelfFollowNotAllowedException();
    }
  }
}
