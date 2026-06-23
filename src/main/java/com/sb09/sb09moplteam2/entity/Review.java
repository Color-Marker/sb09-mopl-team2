package com.sb09.sb09moplteam2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private Double rating;

  @Column(nullable = false, columnDefinition = "text")
  private String text;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
