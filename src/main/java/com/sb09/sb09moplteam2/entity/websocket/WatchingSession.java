package com.sb09.sb09moplteam2.entity.websocket;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watching_sessions",
    indexes = {
        @Index(name = "idx_watching_session_user_id", columnList = "user_id"),
        @Index(name = "idx_watching_session_content_id", columnList = "content_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingSession {

  @Id
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @NotNull
  @Column(name = "content_id", nullable = false)
  private UUID contentId;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private WatchingSessionStatus status;

  @Column(name = "started_at", nullable = false, updatable = false)
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Version
  private Long version;

  public static WatchingSession create(UUID userId, UUID contentId) {
    WatchingSession session = new WatchingSession();
    session.userId = userId;
    session.contentId = contentId;
    session.status = WatchingSessionStatus.ACTIVE;
    session.startedAt = Instant.now();
    return session;
  }

  public void end() {
    this.status = WatchingSessionStatus.ENDED;
    this.endedAt = Instant.now();
  }
}
