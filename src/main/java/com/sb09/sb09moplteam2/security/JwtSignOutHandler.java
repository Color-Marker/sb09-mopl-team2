package com.sb09.sb09moplteam2.security;

import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class JwtSignOutHandler implements LogoutHandler {

  private final JwtSessionRepository jwtSessionRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return;
    }

    for (Cookie cookie : cookies) {
      if ("REFRESH_TOKEN".equals(cookie.getName())) {
        jwtSessionRepository.findByRefreshTokenAndRevokedFalse(cookie.getValue())
            .ifPresent(session -> {
              session.revoke();
              jwtSessionRepository.save(session);
            });
      }
    }
  }
}