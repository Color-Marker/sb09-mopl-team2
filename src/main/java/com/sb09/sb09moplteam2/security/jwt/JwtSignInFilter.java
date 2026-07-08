package com.sb09.sb09moplteam2.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.exception.ErrorResponse;
import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtSignInFilter extends UsernamePasswordAuthenticationFilter {

  private final JwtProvider jwtProvider;
  private final JwtSessionRepository jwtSessionRepository;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final ObjectMapper objectMapper;
  private final SessionBlacklistService sessionBlacklistService;

  public JwtSignInFilter(
      AuthenticationManager authenticationManager,
      JwtProvider jwtProvider,
      JwtSessionRepository jwtSessionRepository,
      UserRepository userRepository,
      UserMapper userMapper,
      ObjectMapper objectMapper,
      SessionBlacklistService sessionBlacklistService
  ) {
    super(authenticationManager);
    this.jwtProvider = jwtProvider;
    this.jwtSessionRepository = jwtSessionRepository;
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.objectMapper = objectMapper;
    this.sessionBlacklistService = sessionBlacklistService;
    setFilterProcessesUrl("/api/auth/sign-in");
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult
  ) throws IOException {
    CustomUserDetails principal = (CustomUserDetails) authResult.getPrincipal();
    User user = userRepository.findById(principal.getId())
        .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

    // 기존 세션 revoke + blacklist
    List<JwtSession> activeSessions = jwtSessionRepository.findAllByUserIdAndRevokedFalse(user.getId());
    activeSessions.forEach(session -> {
      session.revoke();
      sessionBlacklistService.blacklist(session.getId());
    });
    jwtSessionRepository.saveAll(activeSessions);

    // 새 세션 먼저 저장 → sessionId를 access token에 포함
    String refreshToken = jwtProvider.generateRefreshToken(user.getId());
    JwtSession newSession = new JwtSession(
        user.getId(),
        refreshToken,
        Instant.now().plusMillis(jwtProvider.getRefreshTokenExpirationMs())
    );
    jwtSessionRepository.save(newSession);
    String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole(), newSession.getId());

    Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtProvider.getRefreshTokenExpirationMs() / 1000));
    response.addCookie(refreshCookie);

    JwtDto jwtDto = new JwtDto(userMapper.toDto(user), accessToken);

    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), jwtDto);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException failed
  ) throws IOException {
    ErrorResponse errorResponse = new ErrorResponse(
        failed.getClass().getSimpleName(),
        "이메일 또는 비밀번호가 올바르지 않습니다.",
        Map.of()
    );

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}