package com.sb09.sb09moplteam2.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sb09.sb09moplteam2.auth.dto.response.TokenRefreshResult;
import com.sb09.sb09moplteam2.auth.service.AuthService;
import com.sb09.sb09moplteam2.config.JpaAuditingConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.exception.auth.InvalidTokenException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.RefreshTokenCookieFactory;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {QuerydslConfig.class, JpaAuditingConfig.class}
    )
)
@Import({GlobalExceptionHandler.class, RefreshTokenCookieFactory.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private JwtProvider jwtProvider;

  private JwtDto createJwtDto() {
    UserDto userDto = new UserDto(UUID.randomUUID(), Instant.now(), "woody@mopl.io", "우디", null, Role.USER, false);
    return new JwtDto(userDto, "access-token");
  }

  // ── POST /api/auth/refresh ────────────────────────────────────────────────

  @Test
  void 유효한_리프레시_토큰으로_재발급하면_200과_새_토큰을_반환한다() throws Exception {
    TokenRefreshResult result = new TokenRefreshResult(createJwtDto(), "new-refresh-token");

    given(authService.refresh("valid-refresh-token")).willReturn(result);
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(604800000L);

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "valid-refresh-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.userDto.email").value("woody@mopl.io"))
        .andExpect(cookie().exists("REFRESH_TOKEN"));
  }

  @Test
  void 유효하지_않은_리프레시_토큰이면_401을_반환한다() throws Exception {
    given(authService.refresh("invalid-refresh-token")).willThrow(new InvalidTokenException());

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "invalid-refresh-token")))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/auth/csrf-token ──────────────────────────────────────────────

  @Test
  void csrf_토큰_조회시_204를_반환한다() throws Exception {
    mockMvc.perform(get("/api/auth/csrf-token"))
        .andExpect(status().isNoContent());
  }

  // ── POST /api/auth/reset-password ────────────────────────────────────────

  @Test
  void 비밀번호_초기화에_성공하면_204를_반환한다() throws Exception {
    willDoNothing().given(authService).resetPassword("woody@mopl.io");

    mockMvc.perform(post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"woody@mopl.io"}
                """))
        .andExpect(status().isNoContent());
  }

  @Test
  void 존재하지_않는_이메일로_초기화하면_404를_반환한다() throws Exception {
    willThrow(UserNotFoundException.withEmail("none@mopl.io"))
        .given(authService).resetPassword("none@mopl.io");

    mockMvc.perform(post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"none@mopl.io"}
                """))
        .andExpect(status().isNotFound());
  }

  @Test
  void 이메일_형식이_아니면_400을_반환한다() throws Exception {
    mockMvc.perform(post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"invalid-email"}
                """))
        .andExpect(status().isBadRequest());
  }
}