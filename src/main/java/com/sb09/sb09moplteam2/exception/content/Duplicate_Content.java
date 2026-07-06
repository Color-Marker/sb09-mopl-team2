package com.sb09.sb09moplteam2.exception.content;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;

public class Duplicate_Content extends MoplException {

  public Duplicate_Content() {
    super(ErrorCode.DUPLICATE_CONTENT);
  }
}
