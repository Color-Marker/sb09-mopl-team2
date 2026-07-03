package com.sb09.sb09moplteam2.exception.playlist;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class PlaylistForbiddenException extends MoplException {

  public PlaylistForbiddenException() {
    super(ErrorCode.PLAYLIST_FORBIDDEN);
  }
}
