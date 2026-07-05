package com.sb09.sb09moplteam2.exception.playlist;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class PlaylistNotFoundException extends MoplException {

  public PlaylistNotFoundException() {
    super(ErrorCode.PLAYLIST_NOT_FOUND);
  }
}
