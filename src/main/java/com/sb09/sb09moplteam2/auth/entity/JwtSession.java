package com.sb09.sb09moplteam2.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "jwt_sessions")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class JwtSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "refresh_token", nullable = false, length = 1024)
  private String refreshToken;

  @Column(name = "expiration_time", nullable = false)
  private Instant expirationTime;

  @Column(nullable = false)
  private boolean revoked = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public JwtSession(UUID userId, String refreshToken, Instant expirationTime) {
    this.userId = userId;
    this.refreshToken = refreshToken;
    this.expirationTime = expirationTime;
  }

  public void revoke() {
    this.revoked = true;
  }
}