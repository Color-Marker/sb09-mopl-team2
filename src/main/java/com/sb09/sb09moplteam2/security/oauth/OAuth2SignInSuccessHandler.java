package com.sb09.sb09moplteam2.security.oauth;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.RefreshTokenCookieFactory;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
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
  private final SessionBlacklistService sessionBlacklistService;
  private final RefreshTokenCookieFactory refreshTokenCookieFactory;

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
    activeSessions.forEach(session -> {
      session.revoke();
      sessionBlacklistService.blacklist(session.getId());
    });
    jwtSessionRepository.saveAll(activeSessions);

    String refreshToken = jwtProvider.generateRefreshToken(principal.getUserId());
    JwtSession session = new JwtSession(
        principal.getUserId(),
        refreshToken,
        Instant.now().plusMillis(jwtProvider.getRefreshTokenExpirationMs())
    );
    jwtSessionRepository.save(session);

    refreshTokenCookieFactory.addRefreshTokenCookie(response, refreshToken);

    response.sendRedirect(frontendBaseUrl + "/");
  }
}