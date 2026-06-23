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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "playlistItems")
@NoArgsConstructor
public class PlaylistItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID id;

  @Column(name = "order_index", nullable = false)
  private Integer orderIndex;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playlist_id", nullable = false)
  private Playlist playlist_id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content_id;
}
