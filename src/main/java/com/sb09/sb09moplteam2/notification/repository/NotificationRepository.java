package com.sb09.sb09moplteam2.notification.repository;

import com.sb09.sb09moplteam2.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

  Long countByReceiver_Id(UUID receiverId);
}
