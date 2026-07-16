package com.sb09.sb09moplteam2.websocket.event;

import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;

public record WatchingSessionEvent(String type, WatchingSessionDto watchingSession) {

}
