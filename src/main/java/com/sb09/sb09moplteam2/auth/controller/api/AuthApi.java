package com.sb09.sb09moplteam2.auth.controller.api;

import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "인증 관리")
public interface AuthApi {

  @Operation(summary = "토큰 재발급")
  ResponseEntity<JwtDto> refresh(String refreshToken, HttpServletResponse response);

  @Operation(summary = "CSRF 토큰 조회")
  ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);
}