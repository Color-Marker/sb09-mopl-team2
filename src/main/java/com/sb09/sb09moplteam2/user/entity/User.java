package com.sb09.sb09moplteam2.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 30)
  private String name;

  @Column(nullable = false, length = 50, unique = true)
  private String email;

  @Column(length = 100)
  private String password;

  @Column(name = "profile_image_url", length = 255)
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.USER;

  @Column(name = "is_locked", nullable = false)
  private boolean locked = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Provider provider = Provider.LOCAL;

  @Column(name = "provider_id", length = 255)
  private String providerId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  // 로컬 가입
  public User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.provider = Provider.LOCAL;
  }

  // 소셜 가입
  public User(String name, String email, String providerId, Provider provider) {
    this.name = name;
    this.email = email;
    this.providerId = providerId;
    this.provider = provider;
  }

  public void changeRole(Role role) {
    this.role = role;
  }

  public void changePassword(String encodedPassword) {
    this.password = encodedPassword;
  }
}
