package com.sb09.sb09moplteam2.profile.dto.data;

import java.util.UUID;

public record ConversationDto(
    UUID id,
    UserSummary with,
    DirectMessageDto lastestMessage,
    boolean hasUnread
) {}
