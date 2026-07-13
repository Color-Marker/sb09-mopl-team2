package com.sb09.sb09moplteam2.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.dto.request.ContentCreateRequest;
import com.sb09.sb09moplteam2.content.dto.request.ContentUpdateRequest;
import com.sb09.sb09moplteam2.content.dto.response.CursorResponseContentDto;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.service.ContentService;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContentController.class)
@Import(ContentControllerTest.MethodSecurityTestConfig.class)
class ContentControllerTest {

  @TestConfiguration
  @EnableMethodSecurity
  static class MethodSecurityTestConfig {
  }

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ContentService contentService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("콘텐츠 생성 성공")
  @WithMockUser(roles = "ADMIN")
  void 콘텐츠_생성에_성공하면_201을_반환한다() throws Exception {
    UUID contentId = UUID.randomUUID();
    ContentCreateRequest request = new ContentCreateRequest(ContentType.movie, "테스트 영화", "설명", List.of("액션"));
    ContentDto contentDto = new ContentDto(contentId, ContentType.movie, "테스트 영화", "설명", null, List.of("액션"), 0.0, 0, 0L);

    MockMultipartFile requestPart = new MockMultipartFile(
        "request", "", "application/json", objectMapper.writeValueAsBytes(request));

    given(contentService.create(any(ContentCreateRequest.class), any())).willReturn(contentDto);

    mockMvc.perform(multipart("/api/contents")
            .file(requestPart)
            .with(csrf())
            .with(req -> {
              req.setMethod("POST");
              return req;
            }))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("테스트 영화"));
  }

  @Test
  @DisplayName("ADMIN이 아니면 콘텐츠 생성이 403으로 거부된다")
  @WithMockUser(roles = "USER")
  void ADMIN이_아니면_콘텐츠_생성이_거부된다() throws Exception {

    ContentCreateRequest request = new ContentCreateRequest(ContentType.movie, "테스트 영화", "설명", List.of("액션"));
    MockMultipartFile requestPart = new MockMultipartFile(
        "request", "", "application/json", objectMapper.writeValueAsBytes(request));

    mockMvc.perform(multipart("/api/contents")
            .file(requestPart)
            .with(csrf())
            .with(req -> {
              req.setMethod("POST");
              return req;
            }))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("콘텐츠 단건 조회 성공")
  @WithMockUser
  void 콘텐츠_단건_조회에_성공하면_200을_반환한다() throws Exception {
    // given
    UUID contentId = UUID.randomUUID();
    ContentDto contentDto = new ContentDto(contentId, ContentType.movie, "테스트 영화", "설명", null, List.of(), 0.0, 0, 0L);

    given(contentService.findById(contentId)).willReturn(contentDto);

    // when & then
    mockMvc.perform(get("/api/contents/{contentId}", contentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("테스트 영화"));
  }

  @Test
  @DisplayName("콘텐츠 수정 성공")
  @WithMockUser(roles = "ADMIN")
  void 콘텐츠_수정에_성공하면_200을_반환한다() throws Exception {
    UUID contentId = UUID.randomUUID();
    ContentUpdateRequest request = new ContentUpdateRequest("수정된 제목", "수정된 설명", List.of());
    ContentDto contentDto = new ContentDto(contentId, ContentType.movie, "수정된 제목", "수정된 설명", null, List.of(), 0.0, 0, 0L);

    MockMultipartFile requestPart = new MockMultipartFile(
        "request", "", "application/json", objectMapper.writeValueAsBytes(request));

    given(contentService.update(any(UUID.class), any(ContentUpdateRequest.class), any()))
        .willReturn(contentDto);

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/contents/{contentId}", contentId)
            .file(requestPart)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정된 제목"));
  }

  @Test
  @DisplayName("콘텐츠 삭제 성공")
  @WithMockUser(roles = "ADMIN")
  void 콘텐츠_삭제에_성공하면_204를_반환한다() throws Exception {
    // given
    UUID contentId = UUID.randomUUID();
    willDoNothing().given(contentService).delete(any(UUID.class));

    // when & then
    mockMvc.perform(delete("/api/contents/{contentId}", contentId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("콘텐츠 목록 조회 성공")
  @WithMockUser
  void 콘텐츠_목록_조회에_성공하면_200을_반환한다() throws Exception {
    // given
    CursorResponseContentDto response = new CursorResponseContentDto(
        Collections.<ContentSummary>emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"
    );

    given(contentService.findContents(any(), any(), any(), any(), any(), any(), any(), any()))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/contents")
            .param("limit", "10")
            .param("sortDirection", "DESCENDING")
            .param("sortBy", "createdAt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasNext").value(false));
  }
}