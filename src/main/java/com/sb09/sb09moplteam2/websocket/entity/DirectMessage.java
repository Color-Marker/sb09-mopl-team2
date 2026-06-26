package com.sb09.sb09moplteam2.websocket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "direct_messages",
    indexes = {
        @Index(name = "idx_direct_message_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_direct_message_sent_at", columnList = "sent_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessage {

  @Id
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  @NotNull
  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @NotBlank
  @Size(max = 2000)
  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "sent_at", nullable = false, updatable = false)
  private Instant sentAt;

  public static DirectMessage of(Conversation conversation, UUID senderId, String content) {
    DirectMessage dm = new DirectMessage();
    dm.conversation = conversation;
    dm.senderId = senderId;
    dm.content = content;
    dm.sentAt = Instant.now();
    return dm;
  }
}
