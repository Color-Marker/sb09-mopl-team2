package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.exception.websocket.ConversationNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.ConversationParticipantNotFoundException;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.service.ConversationService;
import com.sb09.sb09moplteam2.websocket.service.DirectMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversationController.class)
@Import(GlobalExceptionHandler.class)
class ConversationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ConversationService conversationService;
  @MockitoBean
  private DirectMessageService directMessageService;

  private UUID myUserId;
  private UUID conversationId;

  @BeforeEach
  void setUp() {
    myUserId = UUID.randomUUID();
    conversationId = UUID.randomUUID();
  }

  private RequestPostProcessor userPrincipal(UUID userId) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
        userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    return authentication(token);
  }

  // ───────────────────────────── create ─────────────────────────────

  @Test
  void create_성공_200() throws Exception {
    UUID withUserId = UUID.randomUUID();
    ConversationDto response = mock(ConversationDto.class);
    given(conversationService.createDirect(eq(myUserId), eq(withUserId))).willReturn(response);

    String requestBody = """
        {"withUserId": "%s"}
        """.formatted(withUserId);

    mockMvc.perform(post("/api/conversations")
            .with(userPrincipal(myUserId))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk());
  }

  @Test
  void create_인증없음_302() throws Exception {
    String requestBody = """
        {"withUserId": "%s"}
        """.formatted(UUID.randomUUID());

    mockMvc.perform(post("/api/conversations")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isFound());
  }

  @Test
  void create_withUserId_누락시_400() throws Exception {
    mockMvc.perform(post("/api/conversations")
            .with(userPrincipal(myUserId))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ───────────────────────────── findAll ─────────────────────────────

  @Test
  void findAll_성공_200() throws Exception {
    CursorResponse<ConversationDto> response = mock(CursorResponse.class);
    given(response.totalCount()).willReturn(0L);
    given(conversationService.findAll(eq(myUserId), any(), any(), any(), eq(10), eq("lastMessageAt"), eq("DESCENDING")))
        .willReturn(response);

    mockMvc.perform(get("/api/conversations")
            .with(userPrincipal(myUserId))
            .param("limit", "10")
            .param("sortBy", "lastMessageAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isOk());
  }

  @Test
  void findAll_limit_누락시_400() throws Exception {
    mockMvc.perform(get("/api/conversations")
            .with(userPrincipal(myUserId))
            .param("sortBy", "lastMessageAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void findAll_limit_범위초과시_400() throws Exception {
    mockMvc.perform(get("/api/conversations")
            .with(userPrincipal(myUserId))
            .param("limit", "101")
            .param("sortBy", "lastMessageAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void findAll_인증없음_302() throws Exception {
    mockMvc.perform(get("/api/conversations")
            .param("limit", "10")
            .param("sortBy", "lastMessageAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isFound());
  }

  // ───────────────────────────── findById ─────────────────────────────

  @Test
  void findById_성공_200() throws Exception {
    ConversationDto response = mock(ConversationDto.class);
    given(conversationService.findById(conversationId, myUserId)).willReturn(response);

    mockMvc.perform(get("/api/conversations/{conversationId}", conversationId)
            .with(userPrincipal(myUserId)))
        .andExpect(status().isOk());
  }

  @Test
  void findById_존재하지않으면_404() throws Exception {
    willThrow(new ConversationNotFoundException(conversationId))
        .given(conversationService).findById(conversationId, myUserId);

    mockMvc.perform(get("/api/conversations/{conversationId}", conversationId)
            .with(userPrincipal(myUserId)))
        .andExpect(status().isNotFound());
  }

  @Test
  void findById_인증없음_302() throws Exception {
    mockMvc.perform(get("/api/conversations/{conversationId}", conversationId))
        .andExpect(status().isFound());
  }

  // ───────────────────────────── findWithUser ─────────────────────────────

  @Test
  void findWithUser_성공_200() throws Exception {
    UUID withUserId = UUID.randomUUID();
    ConversationDto response = mock(ConversationDto.class);
    given(conversationService.findWithUser(myUserId, withUserId)).willReturn(response);

    mockMvc.perform(get("/api/conversations/with")
            .with(userPrincipal(myUserId))
            .param("userId", withUserId.toString()))
        .andExpect(status().isOk());
  }

  @Test
  void findWithUser_존재하지않으면_404() throws Exception {
    UUID withUserId = UUID.randomUUID();
    willThrow(new ConversationNotFoundException(null))
        .given(conversationService).findWithUser(myUserId, withUserId);

    mockMvc.perform(get("/api/conversations/with")
            .with(userPrincipal(myUserId))
            .param("userId", withUserId.toString()))
        .andExpect(status().isNotFound());
  }

  // ───────────────────────────── findDms ─────────────────────────────

  @Test
  void findDms_성공_200() throws Exception {
    CursorResponse<DirectMessageDto> response = mock(CursorResponse.class);
    given(response.totalCount()).willReturn(0L);
    given(directMessageService.findAll(
        eq(conversationId), eq(myUserId), any(), any(), eq(20), eq("sentAt"), eq("DESCENDING")))
        .willReturn(response);

    mockMvc.perform(get("/api/conversations/{conversationId}/direct-messages", conversationId)
            .with(userPrincipal(myUserId))
            .param("limit", "20")
            .param("sortBy", "sentAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isOk());
  }

  @Test
  void findDms_참여자가_아니면_403() throws Exception {
    willThrow(new ConversationParticipantNotFoundException(conversationId, myUserId))
        .given(directMessageService).findAll(
            eq(conversationId), eq(myUserId), any(), any(), eq(20), eq("sentAt"), eq("DESCENDING"));

    mockMvc.perform(get("/api/conversations/{conversationId}/direct-messages", conversationId)
            .with(userPrincipal(myUserId))
            .param("limit", "20")
            .param("sortBy", "sentAt")
            .param("sortDirection", "DESCENDING"))
        .andExpect(status().isForbidden());
  }

  // ───────────────────────────── read ─────────────────────────────
  // NOTE: 경로가 /{conversationId}/direct-messages/{directMessageId}/read 에서
  // /{conversationId}/read 로 변경된 것을 반영함

  @Test
  void read_성공_200() throws Exception {
    willDoNothing().given(directMessageService).read(conversationId, myUserId);

    mockMvc.perform(post("/api/conversations/{conversationId}/read", conversationId)
            .with(userPrincipal(myUserId))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void read_대화방이_없으면_404() throws Exception {
    willThrow(new ConversationNotFoundException(conversationId))
        .given(directMessageService).read(conversationId, myUserId);

    mockMvc.perform(post("/api/conversations/{conversationId}/read", conversationId)
            .with(userPrincipal(myUserId))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  void read_인증없음_302() throws Exception {
    mockMvc.perform(post("/api/conversations/{conversationId}/read", conversationId)
            .with(csrf()))
        .andExpect(status().isFound());
  }

}
