package com.sb09.sb09moplteam2.notification.entity;

import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", columnDefinition = "timestamp with time zone", updatable = false, nullable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", columnDefinition = "uuid", nullable = false)
  private User receiver;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", columnDefinition = "uuid")
  private DirectMessage message;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "text")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private NotificationLevel level;

  // dm 알람일 경우
  public Notification(User receiver, DirectMessage message, String title,
      String content) {
    this.receiver = receiver;
    this.message = message;
    this.title = title;
    this.content = content;
    this.type = NotificationType.DM;
    this.level = NotificationLevel.INFO;
  }

  // 이벤트 알람일 경우
  public Notification(User receiver, String title,
      String content, NotificationLevel level) {
    this.receiver = receiver;
    this.message = null;
    this.title = title;
    this.content = content;
    this.type = NotificationType.EVENT;
    this.level = level;
  }
}
