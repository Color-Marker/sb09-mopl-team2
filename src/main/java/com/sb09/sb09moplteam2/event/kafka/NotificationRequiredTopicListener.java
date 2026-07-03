package com.sb09.sb09moplteam2.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.event.message.FollowUserWorkEvent;
import com.sb09.sb09moplteam2.event.message.FollowedEvent;
import com.sb09.sb09moplteam2.event.message.MessageCreatedEvent;
import com.sb09.sb09moplteam2.event.message.RoleUpdatedEvent;
import com.sb09.sb09moplteam2.event.message.SubsPlaylistWorkEvent;
import com.sb09.sb09moplteam2.event.message.SubscribedPlaylistEvent;
import com.sb09.sb09moplteam2.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "mopl.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent){ // 권한 변경
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      notificationService.createRoleUpdateNotification(event.userId(), event.previous(), event.now());
    } catch (JsonProcessingException e){
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "mopl.SubscribedPlaylistEvent")
  public void onSubscribedPlaylistEvent(String kafkaEvent){ // 내 플리가 구독됌
    try {
      SubscribedPlaylistEvent event = objectMapper.readValue(kafkaEvent, SubscribedPlaylistEvent.class);
      notificationService.createSubsNotification(event.userId(),event.subscriberId(), event.playlistId());;
    } catch (JsonProcessingException e){
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "mopl.SubsPlaylistWorkEvent")
  public void onSubsPlaylistWorkEvent(String kafkaEvent){ // 구독한 플리에 컨텐츠 추가됌
    try {
      SubsPlaylistWorkEvent event = objectMapper.readValue(kafkaEvent, SubsPlaylistWorkEvent.class);
      notificationService.createSubsWorkNotification(event.userIds(), event.playlistId());
    } catch (JsonProcessingException e){
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "mopl.FollowUserWorkEvent")
  public void onFollowUserWorkEvent(String kafkaEvent){ // 내가 팔로우해둔 사람이 새로운 플레이리스트 만듦
    try {
      FollowUserWorkEvent event = objectMapper.readValue(kafkaEvent, FollowUserWorkEvent.class);
      notificationService.createFollowWorkNotification(event.userIds(), event.followedId(), event.playlistId());
    } catch (JsonProcessingException e){
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "mopl.FollowedEvent")
  public void onFollowedEvent(String kafkaEvent){ // 날 누군가가 팔로우함
    try {
      FollowedEvent event = objectMapper.readValue(kafkaEvent, FollowedEvent.class);
      notificationService.createFollowNotification(event.userId(), event.followerId());
    } catch (JsonProcessingException e){
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "mopl.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent){ // DM 메시지가 옴
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      notificationService.createDmNotification(event.userId(), event.messageDto());
    } catch (JsonProcessingException e){
      throw new RuntimeException(e);
    }
  }
}
