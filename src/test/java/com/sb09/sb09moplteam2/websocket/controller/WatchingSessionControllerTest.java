package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.service.WatchingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WatchingSessionController.class)
@Import(GlobalExceptionHandler.class)
class WatchingSessionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WatchingSessionService watchingSessionService;

  private UUID watcherId;
  private UUID contentId;

  @BeforeEach
  void setUp() {
    watcherId = UUID.randomUUID();
    contentId = UUID.randomUUID();
  }

  // ───────────────────────────── findByWatcher ─────────────────────────────

  @Test
  void findByWatcher_활성_세션이_있으면_200과_바디를_반환한다() throws Exception {
    WatchingSessionDto response = mock(WatchingSessionDto.class);
    given(watchingSessionService.findActiveByUserId(watcherId)).willReturn(response);

    mockMvc.perform(get("/api/users/{watcherId}/watching-sessions", watcherId)

            .with(user("testUser").roles("USER")))

        .andExpect(status().isOk());
  }

  @Test
  void findByWatcher_활성_세션이_없으면_200과_빈_바디를_반환한다() throws Exception {
    given(watchingSessionService.findActiveByUserId(watcherId)).willReturn(null);

    mockMvc.perform(get("/api/users/{watcherId}/watching-sessions", watcherId)
            .with(user("testUser").roles("USER")))

        .andExpect(status().isOk())
        .andExpect(content().string(""));
  }

  // ───────────────────────────── findByContent ─────────────────────────────

  @Test
  void findByContent_성공_200() throws Exception {
    CursorResponse<WatchingSessionDto> response = mock(CursorResponse.class);
    given(response.totalCount()).willReturn(0L);
    given(watchingSessionService.findAllByContentId(
        eq(contentId), any(), any(), eq(20), eq("startedAt"), eq("DESCENDING")))
        .willReturn(response);

    mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)

            .with(user("testUser").roles("USER"))

            .param("limit", "20")
            .param("sortBy", "startedAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isOk());
  }

  @Test
  void findByContent_limit_누락시_400() throws Exception {
    mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)
            .with(user("testUser").roles("USER"))

            .param("sortBy", "startedAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void findByContent_limit_범위초과시_400() throws Exception {
    mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)

            .with(user("testUser").roles("USER"))

            .param("limit", "101")
            .param("sortBy", "startedAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void findByContent_limit_0이하이면_400() throws Exception {
    mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)

            .with(user("testUser").roles("USER"))

            .param("limit", "0")
            .param("sortBy", "startedAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void findByContent_sortBy_누락시_400() throws Exception {
    mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)

            .with(user("testUser").roles("USER"))

            .param("limit", "20")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void findByContent_sortBy_공백이면_400() throws Exception {
    mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)

            .with(user("testUser").roles("USER"))

            .param("limit", "20")
            .param("sortBy", "   ")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }
}
