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
@Table(name = "playlists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, columnDefinition = "text")
  private String description;

  @Column(name = "subscriber_count", nullable = false)
  private Long subscriberCount = 0L;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;
}