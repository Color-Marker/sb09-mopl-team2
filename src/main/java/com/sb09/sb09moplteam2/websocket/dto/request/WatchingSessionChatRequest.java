package com.sb09.sb09moplteam2.websocket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WatchingSessionChatRequest(
    @NotBlank
    @Size(max = 500)
    String content
) {
}
