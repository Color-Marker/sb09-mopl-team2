package com.sb09.sb09moplteam2.event.message;

import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import java.util.UUID;

public record MessageCreatedEvent(
    UUID userId,
    DirectMessageDto messageDto
) {

}
