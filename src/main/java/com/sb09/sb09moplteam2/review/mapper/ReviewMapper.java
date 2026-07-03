package com.sb09.sb09moplteam2.review.mapper;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.review.dto.data.ReviewDto;
import com.sb09.sb09moplteam2.review.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

  public ReviewDto toDto(Review review) {
    return new ReviewDto(
        review.getId(),
        review.getContent().getId(),
        new UserSummary(
            review.getUser().getId(),
            review.getUser().getName(),
            review.getUser().getProfileImageUrl()
        ),
        review.getText(),
        review.getRating()
    );
  }
}