package com.sb09.sb09moplteam2.follow.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.follow.dto.data.FollowDto;
import com.sb09.sb09moplteam2.follow.dto.request.FollowRequest;
import com.sb09.sb09moplteam2.follow.service.FollowService;
import com.sb09.sb09moplteam2.security.jwt.CustomUserDetails;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FollowController.class)
class FollowControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private FollowService followService;

  @Test
  @DisplayName("팔로우 요청 시 201 Created 상태 코드를 반환한다.")
  void follow_ReturnsCreated() throws Exception {
    // given
    UUID currentUserId = UUID.randomUUID();
    FollowRequest request = new FollowRequest(UUID.randomUUID());

    // CustomUserDetails 가짜 객체(Mock) 생성 및 ID 부여
    CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
    given(mockUserDetails.getId()).willReturn(currentUserId);

    // when & then
    mockMvc.perform(post("/api/follows")
            .with(csrf())
            .with(user(mockUserDetails))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("팔로우 여부 조회 시 FollowDto 객체와 200 OK를 반환한다.")
  void checkFollowingStatus_ReturnsOk() throws Exception {
    // given
    UUID currentUserId = UUID.randomUUID();
    UUID followeeId = UUID.randomUUID();
    UUID followId = UUID.randomUUID();

    FollowDto mockFollowDto = new FollowDto(followId, followeeId, currentUserId);

    given(followService.getFollowDetails(currentUserId, followeeId)).willReturn(mockFollowDto);

    Authentication auth = new UsernamePasswordAuthenticationToken(currentUserId, null, Collections.emptyList());

    // when & then
    mockMvc.perform(get("/api/follows/followed-by-me")
            .param("followeeId", followeeId.toString())
            .with(authentication(auth)) // 💡 user(...) 대신 authentication(auth)로 주입
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(followId.toString()))
        .andExpect(jsonPath("$.followeeId").value(followeeId.toString()))
        .andExpect(jsonPath("$.followerId").value(currentUserId.toString()));
  }
}
