package com.sb09.sb09moplteam2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

  @Id
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 10)
  private ConversationType type;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Version
  private Long version;

  public static Conversation createDirect() {
    Conversation conversation = new Conversation();
    conversation.type = ConversationType.DIRECT;
    conversation.createdAt = Instant.now();
    return conversation;
  }

  public static Conversation createGroup(String name) {
    Conversation conversation = new Conversation();
    conversation.type = ConversationType.GROUP;
    conversation.name = name;
    conversation.createdAt = Instant.now();
    return conversation;
  }

  public void updateName(String name) {
    this.name = name;
  }
}
