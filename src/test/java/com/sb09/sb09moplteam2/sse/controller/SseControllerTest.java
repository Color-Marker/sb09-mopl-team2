package com.sb09.sb09moplteam2.sse.controller;


import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.security.CustomUserDetails;
import com.sb09.sb09moplteam2.sse.SseController;
import com.sb09.sb09moplteam2.sse.SseService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SseController.class)
@Import(GlobalExceptionHandler.class)
class SseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SseService sseService;

  private UUID userId;
  private CustomUserDetails principal;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    principal = mock(CustomUserDetails.class);
    given(principal.getId()).willReturn(userId);
  }

  @Test
  void subscribe_LastEventId_없음_성공() throws Exception {
    SseEmitter emitter = new SseEmitter();
    given(sseService.connect(eq(userId), eq(null))).willReturn(emitter);

    mockMvc.perform(get("/api/sse")
            .with(user(principal)))
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk());

    verify(sseService).connect(userId, null);
  }

  @Test
  void subscribe_LastEventId_있음_성공() throws Exception {
    UUID lastEventId = UUID.randomUUID();
    SseEmitter emitter = new SseEmitter();
    given(sseService.connect(eq(userId), eq(lastEventId))).willReturn(emitter);

    mockMvc.perform(get("/api/sse")
            .with(user(principal))
            .param("LastEventId", lastEventId.toString()))
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk());

    verify(sseService).connect(userId, lastEventId);
  }

  @Test
  void subscribe_LastEventId_형식이상_400() throws Exception {
    mockMvc.perform(get("/api/sse")
            .with(user(principal))
            .param("LastEventId", "not-a-uuid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void subscribe_인증없음_401() throws Exception {
    mockMvc.perform(get("/api/sse"))
        .andExpect(status().isUnauthorized());
  }
}