package com.sb09.sb09moplteam2.notification.mapper;

import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  @Mapping(source = "receiver.id", target = "receiverId")
  NotificationDto toDto(Notification notification);
}
