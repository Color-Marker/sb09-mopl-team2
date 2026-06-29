package com.sb09.sb09moplteam2.playlist.entity;


import com.sb09.sb09moplteam2.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@NoArgsConstructor
@Table(name = "playlistSubscriptions")
public class PlaylistSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID id;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToOne
  @JoinColumn(name = "playlist_id", nullable = false)
  private Playlist playlist_id;

  @ManyToOne
  @JoinColumn(name = "subscrber_id", nullable = false)
  private User subscrber_id;
}
