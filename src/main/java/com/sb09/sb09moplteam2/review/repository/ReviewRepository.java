package com.sb09.sb09moplteam2.review.repository;

import com.sb09.sb09moplteam2.review.entity.Review;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {
  boolean existsByContentIdAndUserId(UUID contentId, UUID userId);
  List<Review> findByContentId(UUID contentId);
}
