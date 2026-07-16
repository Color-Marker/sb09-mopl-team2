package com.sb09.sb09moplteam2.event.message;

import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import java.time.Instant;


public class DmEvent extends CreatedEvent<DirectMessageDto>{
  public DmEvent(DirectMessageDto data, Instant createdAt){
    super(data, createdAt);
  }
}
