package com.sb09.sb09moplteam2.security.oauth;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SignInSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final JwtSessionRepository jwtSessionRepository;

  @Value("${mopl.frontend.base-url}")
  private String frontendBaseUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {
    CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

    List<JwtSession> activeSessions = jwtSessionRepository.findAllByUserIdAndRevokedFalse(principal.getUserId());
    activeSessions.forEach(JwtSession::revoke);
    jwtSessionRepository.saveAll(activeSessions);

    String refreshToken = jwtProvider.generateRefreshToken(principal.getUserId());
    JwtSession session = new JwtSession(
        principal.getUserId(),
        refreshToken,
        Instant.now().plusMillis(jwtProvider.getRefreshTokenExpirationMs())
    );
    jwtSessionRepository.save(session);

    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtProvider.getRefreshTokenExpirationMs() / 1000));
    response.addCookie(refreshCookie);

    response.sendRedirect(frontendBaseUrl + "/");
  }
}