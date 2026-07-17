package com.sb09.sb09moplteam2.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sb09.sb09moplteam2.config.JpaAuditingConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.exception.user.DuplicateEmailException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {QuerydslConfig.class, JpaAuditingConfig.class}
    )
)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  private UserDto createUserDto(UUID id) {
    return new UserDto(id, Instant.now(), "woody@mopl.io", "우디", null, Role.USER, false);
  }

  // ── POST /api/users ───────────────────────────────────────────────────────

  @Test
  void 회원가입_성공시_201과_사용자정보를_반환한다() throws Exception {
    UserDto response = createUserDto(UUID.randomUUID());
    given(userService.createUser(any())).willReturn(response);

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "우디",
                  "email": "woody@mopl.io",
                  "password": "mopl1!@#$"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("woody@mopl.io"))
        .andExpect(jsonPath("$.name").value("우디"));
  }

  @Test
  void 이메일이_중복되면_400을_반환한다() throws Exception {
    given(userService.createUser(any())).willThrow(DuplicateEmailException.withEmail("woody@mopl.io"));

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "우디",
                  "email": "woody@mopl.io",
                  "password": "mopl1!@#$"
                }
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 입력값이_유효하지_않으면_400을_반환한다() throws Exception {
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "",
                  "email": "invalid-email",
                  "password": "1234567"
                }
                """))
        .andExpect(status().isBadRequest());
  }

  // ── GET /api/users ────────────────────────────────────────────────────────

  @Test
  void 사용자_목록_조회에_성공하면_200과_CursorResponse를_반환한다() throws Exception {
    CursorResponse<UserDto> response = new CursorResponse<>(
        List.of(createUserDto(UUID.randomUUID())), null, null, false, 1L, "createdAt", "ASCENDING"
    );
    given(userService.findUsers(any())).willReturn(response);

    mockMvc.perform(get("/api/users")
            .param("limit", "10")
            .param("sortDirection", "ASCENDING")
            .param("sortBy", "createdAt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  // ── GET /api/users/{userId} ───────────────────────────────────────────────

  @Test
  void 사용자_단건_조회에_성공하면_200과_UserDto를_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();
    given(userService.findUser(userId)).willReturn(createUserDto(userId));

    mockMvc.perform(get("/api/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("woody@mopl.io"));
  }

  @Test
  void 존재하지_않는_사용자_조회시_404를_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();
    given(userService.findUser(userId)).willThrow(UserNotFoundException.withId(userId));

    mockMvc.perform(get("/api/users/{userId}", userId))
        .andExpect(status().isNotFound());
  }

  // ── PATCH /api/users/{userId} (multipart) ────────────────────────────────

  @Test
  void 프로필_수정에_성공하면_200과_수정된_UserDto를_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();
    UserDto updated = new UserDto(userId, Instant.now(), "woody@mopl.io", "새이름", null, Role.USER, false);

    given(userService.updateUser(eq(userId), any(), any())).willReturn(updated);

    MockMultipartFile requestPart = new MockMultipartFile(
        "request", "", MediaType.APPLICATION_JSON_VALUE,
        """
        {"name":"새이름"}
        """.getBytes(StandardCharsets.UTF_8)
    );

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId)
            .file(requestPart))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("새이름"));
  }

  // ── PATCH /api/users/{userId}/role ───────────────────────────────────────

  @Test
  void 권한_변경에_성공하면_204를_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).updateRole(any(), any());

    mockMvc.perform(patch("/api/users/{userId}/role", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"role":"ADMIN"}
                """))
        .andExpect(status().isNoContent());
  }

  @Test
  void 권한_변경시_role이_null이면_400을_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc.perform(patch("/api/users/{userId}/role", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {}
                """))
        .andExpect(status().isBadRequest());
  }

  // ── PATCH /api/users/{userId}/locked ─────────────────────────────────────

  @Test
  void 계정_잠금_변경에_성공하면_204를_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).updateLocked(any(), eq(true));

    mockMvc.perform(patch("/api/users/{userId}/locked", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"locked":true}
                """))
        .andExpect(status().isNoContent());
  }

  @Test
  void 계정_잠금_변경시_locked가_null이면_400을_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc.perform(patch("/api/users/{userId}/locked", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {}
                """))
        .andExpect(status().isBadRequest());
  }

  // ── PATCH /api/users/{userId}/password ───────────────────────────────────

  @Test
  void 비밀번호_변경에_성공하면_204를_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).changePassword(eq(userId), any());

    mockMvc.perform(patch("/api/users/{userId}/password", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"password":"newPass1!@#"}
                """))
        .andExpect(status().isNoContent());
  }

  @Test
  void 비밀번호_변경시_비밀번호가_null이면_400을_반환한다() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc.perform(patch("/api/users/{userId}/password", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {}
                """))
        .andExpect(status().isBadRequest());
  }
}
