package com.sb09.sb09moplteam2.notification.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.exception.notification.NotificationForbiddenException;
import com.sb09.sb09moplteam2.exception.notification.NotificationNotFoundException;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  private UUID userId;


  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
  }

  private RequestPostProcessor userPrincipal(UUID userId) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
        userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    return authentication(token);
  }

  @Test
  void list_성공_200() throws Exception {
    CursorResponse<NotificationDto> response = mock(CursorResponse.class);
    given(response.totalCount()).willReturn(0L);
    given(notificationService.list(eq(userId), any(NotificationListRequest.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/notifications")
            .with(userPrincipal(userId))
            .param("sortBy", "createdAt")
            .param("sortDirection", "ASCENDING")
            .param("limit", "10"))
        .andExpect(status().isOk());
  }


  @Test
  void list_인증없음_401() throws Exception {
    mockMvc.perform(get("/api/notifications")
            .param("sortBy", "createdAt")
            .param("sortDirection", "ASCENDING")
            .param("limit", "10"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void delete_성공_204() throws Exception {
    UUID notificationId = UUID.randomUUID();
    willDoNothing().given(notificationService).delete(notificationId, userId);

    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(userPrincipal(userId))
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_존재하지않는알림_404() throws Exception {
    UUID notificationId = UUID.randomUUID();
    willThrow(NotificationNotFoundException.withId(notificationId))
        .given(notificationService).delete(notificationId, userId);

    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(userPrincipal(userId))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_권한없음_403() throws Exception {
    UUID notificationId = UUID.randomUUID();
    willThrow(NotificationForbiddenException.withId(notificationId, userId))
        .given(notificationService).delete(notificationId, userId);

    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(userPrincipal(userId))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void delete_인증없음_401() throws Exception {
    UUID notificationId = UUID.randomUUID();

    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}