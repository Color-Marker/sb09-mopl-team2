package com.sb09.sb09moplteam2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 20)
  private String type;

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

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}