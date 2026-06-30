package com.sb09.sb09moplteam2.websocket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
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

  // 마지막 메시지 시각 (목록 정렬/커서 페이지네이션 기준)
  // 현재는 createdAt으로 초기화만 하고, DM 전송 기능 구현 시 메시지 저장 시점에 갱신 예정
  @NotNull
  @Column(name = "last_message_at", nullable = false)
  private Instant lastMessageAt;

  @Version
  private Long version;

  public static Conversation createDirect() {
    Conversation conversation = new Conversation();
    conversation.type = ConversationType.DIRECT;
    conversation.createdAt = Instant.now();
    conversation.lastMessageAt = conversation.createdAt;
    return conversation;
  }

  public static Conversation createGroup(String name) {
    Conversation conversation = new Conversation();
    conversation.type = ConversationType.GROUP;
    conversation.name = name;
    conversation.createdAt = Instant.now();
    conversation.lastMessageAt = conversation.createdAt;
    return conversation;
  }

  public void updateName(String name) {
    this.name = name;
  }

  // DM 전송 기능 구현 시 메시지 저장 시점에 호출 예정
  public void updateLastMessageAt(Instant lastMessageAt) {
    this.lastMessageAt = lastMessageAt;
  }
}
