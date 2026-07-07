package com.sb09.sb09moplteam2.playlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistCreatedRequest;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistUpdateRequest;
import com.sb09.sb09moplteam2.playlist.dto.response.CursorResponsePlaylistDto;
import com.sb09.sb09moplteam2.playlist.service.PlaylistService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(PlaylistController.class)
class PlaylistControllerTest {

  private static final UUID TEST_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private PlaylistService playlistService;

  private RequestPostProcessor mockUser() {
    return authentication(new UsernamePasswordAuthenticationToken(
        TEST_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void 플레이리스트_생성에_성공하면_201을_반환한다() throws Exception {
    PlaylistCreatedRequest request = new PlaylistCreatedRequest("제목", "설명");
    PlaylistDto dto = mock(PlaylistDto.class);
    given(playlistService.create(any(PlaylistCreatedRequest.class), eq(TEST_USER_ID)))
        .willReturn(dto);

    mockMvc.perform(post("/api/playlists")
            .with(csrf())
            .with(mockUser())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    then(playlistService).should().create(any(PlaylistCreatedRequest.class), eq(TEST_USER_ID));
  }

  @Test
  void 플레이리스트_목록_조회에_성공하면_200을_반환한다() throws Exception {

    CursorResponsePlaylistDto response = mock(CursorResponsePlaylistDto.class);
    given(playlistService.findAll(
        any(), any(), any(), any(), any(), anyInt(), any(), any(), eq(TEST_USER_ID)))
        .willReturn(response);

    mockMvc.perform(get("/api/playlists")
            .with(mockUser())
            .param("limit", "10")
            .param("sortDirection", "DESCENDING")
            .param("sortBy", "updatedAt"))
        .andExpect(status().isOk());

    then(playlistService).should().findAll(
        any(), any(), any(), any(), any(), eq(10), eq("DESCENDING"), eq("updatedAt"), eq(TEST_USER_ID));
  }

  @Test
  void 로그인하지_않으면_플레이리스트_목록_조회가_거부된다() throws Exception {
    mockMvc.perform(get("/api/playlists")
            .param("limit", "10")
            .param("sortDirection", "DESCENDING")
            .param("sortBy", "updatedAt"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void 플레이리스트_단건_조회에_성공하면_200을_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();
    PlaylistDto dto = mock(PlaylistDto.class);
    given(playlistService.findById(playlistId, TEST_USER_ID)).willReturn(dto);

    mockMvc.perform(get("/api/playlists/{playlistId}", playlistId)
            .with(mockUser()))
        .andExpect(status().isOk());

    then(playlistService).should().findById(playlistId, TEST_USER_ID);
  }

  @Test
  void 플레이리스트_수정에_성공하면_200을_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();
    PlaylistUpdateRequest request = new PlaylistUpdateRequest("새제목", "새설명");
    PlaylistDto dto = mock(PlaylistDto.class);
    given(playlistService.update(eq(playlistId), any(PlaylistUpdateRequest.class), eq(TEST_USER_ID)))
        .willReturn(dto);

    mockMvc.perform(patch("/api/playlists/{playlistId}", playlistId)
            .with(csrf())
            .with(mockUser())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    then(playlistService).should()
        .update(eq(playlistId), any(PlaylistUpdateRequest.class), eq(TEST_USER_ID));
  }

  @Test
  void 플레이리스트_삭제에_성공하면_204를_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();

    mockMvc.perform(delete("/api/playlists/{playlistId}", playlistId)
            .with(csrf())
            .with(mockUser()))
        .andExpect(status().isNoContent());

    then(playlistService).should().delete(playlistId, TEST_USER_ID);
  }

  @Test
  void 플레이리스트에_콘텐츠_추가에_성공하면_201을_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();

    mockMvc.perform(post("/api/playlists/{playlistId}/contents/{contentId}", playlistId, contentId)
            .with(csrf())
            .with(mockUser()))
        .andExpect(status().isCreated());

    then(playlistService).should().addContent(playlistId, contentId, TEST_USER_ID);
  }

  @Test
  void 플레이리스트에서_콘텐츠_삭제에_성공하면_204를_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();

    mockMvc.perform(delete("/api/playlists/{playlistId}/contents/{contentId}", playlistId, contentId)
            .with(csrf())
            .with(mockUser()))
        .andExpect(status().isNoContent());

    then(playlistService).should().removeContent(playlistId, contentId, TEST_USER_ID);
  }

  @Test
  void 플레이리스트_구독에_성공하면_201을_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();

    mockMvc.perform(post("/api/playlists/{playlistId}/subscription", playlistId)
            .with(csrf())
            .with(mockUser()))
        .andExpect(status().isCreated());

    then(playlistService).should().subscribe(playlistId, TEST_USER_ID);
  }

  @Test
  void 플레이리스트_구독_취소에_성공하면_204를_반환한다() throws Exception {
    UUID playlistId = UUID.randomUUID();

    mockMvc.perform(delete("/api/playlists/{playlistId}/subscription", playlistId)
            .with(csrf())
            .with(mockUser()))
        .andExpect(status().isNoContent());

    then(playlistService).should().unsubscribe(playlistId, TEST_USER_ID);
  }
}