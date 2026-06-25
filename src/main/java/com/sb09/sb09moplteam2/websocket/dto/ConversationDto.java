package com.sb09.sb09moplteam2.websocket.dto;

import com.sb09.sb09moplteam2.dto.UserSummary;

import java.util.UUID;

public record ConversationDto(
    UUID id,
    UserSummary with,
    DirectMessageDto latestMessage,
    boolean hasUnread
) {}
