package com.sb09.sb09moplteam2.event.message;

import java.util.Set;
import java.util.UUID;

public record FollowUserWorkEvent(
    Set<UUID> userIds,
    UUID followedId,
    UUID playlistId
) {

}
