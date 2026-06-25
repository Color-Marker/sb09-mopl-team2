package com.sb09.sb09moplteam2.content.repository;

import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import java.util.List;
import java.util.UUID;

public interface ContentRepositoryCustom  {
  CursorResponseContentDto findContentsWithCursor(
      String typeEqual,
      String keywordLike,
      List<String> tagsIn,
      String cursor,
      UUID idAfter,
      Integer limit,
      String sortDirection,
      String sortBy
  );
}