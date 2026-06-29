package com.sb09.sb09moplteam2.auth.controller;

import com.sb09.sb09moplteam2.auth.controller.api.AuthApi;
import com.sb09.sb09moplteam2.auth.dto.request.ResetPasswordRequest;
import com.sb09.sb09moplteam2.auth.dto.response.TokenRefreshResult;
import com.sb09.sb09moplteam2.auth.service.AuthService;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final JwtProvider jwtProvider;

  @Override
  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue("REFRESH_TOKEN") String refreshToken,
      HttpServletResponse response
  ) {
    TokenRefreshResult result = authService.refresh(refreshToken);

    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", result.refreshToken());
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtProvider.getRefreshTokenExpirationMs() / 1000));
    response.addCookie(refreshCookie);

    return ResponseEntity.ok(result.jwtDto());
  }

  @Override
  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken() {
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.email());
    return ResponseEntity.noContent().build();
  }
}