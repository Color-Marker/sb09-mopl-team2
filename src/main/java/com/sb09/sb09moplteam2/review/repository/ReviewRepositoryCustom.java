package com.sb09.sb09moplteam2.review.repository;

import com.sb09.sb09moplteam2.review.entity.Review;
import java.util.List;
import java.util.UUID;

public interface ReviewRepositoryCustom {
  List<Review> findReviewsWithCursor(
      UUID contentId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  );
}
