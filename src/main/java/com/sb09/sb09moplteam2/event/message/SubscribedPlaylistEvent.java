package com.sb09.sb09moplteam2.event.message;

import java.util.UUID;

public record SubscribedPlaylistEvent(
    UUID subscriberId,
    UUID playlistId
) {

}
