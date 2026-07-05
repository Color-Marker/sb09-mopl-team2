package com.sb09.sb09moplteam2.event.message;

import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.UUID;

public record RoleUpdatedEvent(
  UUID userId,
  Role previous,
  Role now
) {

}
