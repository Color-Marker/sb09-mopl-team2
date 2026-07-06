package com.sb09.sb09moplteam2.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Content {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private ContentType type;

  @Column(name = "external_id", nullable = false, length = 1024)
  private String externalId;

  @Column(nullable = false, length = 50)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "thumbnail_url", length = 255)
  private String thumbnailUrl;

  @Column(name = "release_date")
  private LocalDate releaseDate;

  @Column(length = 10)
  private String status;

  @Column(name = "average_rating", nullable = false)
  private Double averageRating = 0.0;

  @Column(name = "review_count", nullable = false)
  private Integer reviewCount = 0;

  @Column(name = "watcher_count", nullable = false)
  private Long watcherCount = 0L;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Content(ContentType type, String externalId, String title, String description,
      String thumbnailUrl, LocalDate releaseDate, String status) {
    this.type = type;
    this.externalId = externalId;
    this.title = title;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.releaseDate = releaseDate;
    this.status = status;
    this.averageRating = 0.0;
    this.reviewCount = 0;
    this.watcherCount = 0L;
  }

  public void update(String title, String description) {
    if (title != null) this.title = title;
    if (description != null) this.description = description;
  }

  public void updateReviewStats(double averageRating, int reviewCount) {
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
  }

  public void updateWatcherCount(Long watcherCount) {
    this.watcherCount = watcherCount;
  }
}