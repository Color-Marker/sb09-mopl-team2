package com.sb09.sb09moplteam2.review.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.sb09.sb09moplteam2.content.search.ContentSearchService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.review.dto.data.ReviewDto;
import com.sb09.sb09moplteam2.review.dto.request.ReviewCreateRequest;
import com.sb09.sb09moplteam2.review.dto.request.ReviewUpdateRequest;
import com.sb09.sb09moplteam2.review.dto.response.CursorResponseReviewDto;
import com.sb09.sb09moplteam2.review.service.ReviewService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReviewService reviewService;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ContentSearchService contentSearchService;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private RequestPostProcessor mockUser() {
    return authentication(new UsernamePasswordAuthenticationToken(
        TEST_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  @DisplayName("리뷰 생성 성공")
  void 리뷰_생성에_성공하면_201을_반환한다() throws Exception {
    UUID contentId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    ReviewCreateRequest request = new ReviewCreateRequest(contentId, "좋아요", 4.5);
    ReviewDto reviewDto = new ReviewDto(reviewId, contentId, null, "좋아요", 4.5);

    given(reviewService.create(any(ReviewCreateRequest.class), eq(TEST_USER_ID)))
        .willReturn(reviewDto);

    mockMvc.perform(post("/api/reviews")
            .with(csrf())
            .with(mockUser())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.text").value("좋아요"))
        .andExpect(jsonPath("$.rating").value(4.5));
  }

  @Test
  @DisplayName("리뷰 수정 성공")
  void 리뷰_수정에_성공하면_200을_반환한다() throws Exception {
    UUID reviewId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 리뷰", 3.0);
    ReviewDto reviewDto = new ReviewDto(reviewId, contentId, null, "수정된 리뷰", 3.0);

    given(reviewService.update(any(UUID.class), any(ReviewUpdateRequest.class), eq(TEST_USER_ID)))
        .willReturn(reviewDto);

    mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
            .with(csrf())
            .with(mockUser())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.text").value("수정된 리뷰"))
        .andExpect(jsonPath("$.rating").value(3.0));
  }

  @Test
  @DisplayName("리뷰 삭제 성공")
  void 리뷰_삭제에_성공하면_204를_반환한다() throws Exception {
    UUID reviewId = UUID.randomUUID();
    willDoNothing().given(reviewService).delete(any(UUID.class), any(UUID.class));

    mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
            .with(csrf())
            .with(mockUser()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("리뷰 목록 조회 성공")
  @WithMockUser
  void 리뷰_목록_조회에_성공하면_200을_반환한다() throws Exception {
    UUID contentId = UUID.randomUUID();
    CursorResponseReviewDto response = new CursorResponseReviewDto(
        List.of(), null, null, false, 0L, "createdAt", "DESCENDING"
    );

    given(reviewService.findAll(any(), any(), any(), anyInt(), any(), any()))
        .willReturn(response);

    mockMvc.perform(get("/api/reviews")
            .param("contentId", contentId.toString())
            .param("limit", "10")
            .param("sortDirection", "DESCENDING")
            .param("sortBy", "createdAt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.sortBy").value("createdAt"));
  }
}