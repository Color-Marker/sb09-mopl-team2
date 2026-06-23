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
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentTag {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 50)
  private String tag;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;
}