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
@Table(name = "conversations_participants",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_participant_conversation_user",
            columnNames = {"conversation_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_participant_user_id", columnList = "user_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationParticipant {

  @Id
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @Column(name = "last_read_at")
  private Instant lastReadAt;

  public static ConversationParticipant of(Conversation conversation, UUID userId) {
    ConversationParticipant participant = new ConversationParticipant();
    participant.conversation = conversation;
    participant.userId = userId;
    participant.joinedAt = Instant.now();
    return participant;
  }

  public void updateLastReadAt() {
    this.lastReadAt = Instant.now();
  }
}
