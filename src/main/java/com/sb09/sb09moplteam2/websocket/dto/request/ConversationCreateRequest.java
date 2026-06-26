package com.sb09.sb09moplteam2.websocket.dto.request;

import java.util.UUID;

public record ConversationCreateRequest(
    UUID withUserId
) {}
