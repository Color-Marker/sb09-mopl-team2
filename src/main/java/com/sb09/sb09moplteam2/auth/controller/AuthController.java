package com.sb09.sb09moplteam2.auth.controller;

import com.sb09.sb09moplteam2.auth.controller.api.AuthApi;
import com.sb09.sb09moplteam2.auth.dto.request.ResetPasswordRequest;
import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.service.AuthService;
import com.sb09.sb09moplteam2.exception.auth.InvalidTokenException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
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

  private final JwtProvider jwtProvider;
  private final JwtSessionRepository jwtSessionRepository;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final AuthService authService;

  @Override
  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue("REFRESH_TOKEN") String refreshToken,
      HttpServletResponse response
  ) {
    if (!jwtProvider.isValidRefreshToken(refreshToken)) {
      throw new InvalidTokenException();
    }

    JwtSession session = jwtSessionRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
        .orElseThrow(InvalidTokenException::new);
    session.revoke();
    jwtSessionRepository.save(session);

    UUID userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
    String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());

    JwtSession newSession = new JwtSession(
        user.getId(),
        newRefreshToken,
        Instant.now().plusMillis(jwtProvider.getRefreshTokenExpirationMs())
    );
    jwtSessionRepository.save(newSession);

    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", newRefreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtProvider.getRefreshTokenExpirationMs() / 1000));
    response.addCookie(refreshCookie);

    return ResponseEntity.ok(new JwtDto(userMapper.toDto(user), newAccessToken));
  }

  @Override
  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    csrfToken.getToken();
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.email());
    return ResponseEntity.noContent().build();
  }
}