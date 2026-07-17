package com.sb09.sb09moplteam2.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "password_reset_tokens",
    indexes = {
        @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id")
    })
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "temp_password", nullable = false, length = 255)
  private String tempPassword;

  @Column(name = "expiry_date", nullable = false)
  private Instant expiryDate;

  @Column(nullable = false)
  private boolean used = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public PasswordResetToken(UUID userId, String tempPassword, Instant expiryDate) {
    this.userId = userId;
    this.tempPassword = tempPassword;
    this.expiryDate = expiryDate;
  }

  public void markUsed() {
    this.used = true;
  }
}