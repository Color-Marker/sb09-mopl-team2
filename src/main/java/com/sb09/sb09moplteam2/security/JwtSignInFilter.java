package com.sb09.sb09moplteam2.security;

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

  public JwtSignInFilter(
      AuthenticationManager authenticationManager,
      JwtProvider jwtProvider,
      JwtSessionRepository jwtSessionRepository,
      UserRepository userRepository,
      UserMapper userMapper,
      ObjectMapper objectMapper
  ) {
    super(authenticationManager);
    this.jwtProvider = jwtProvider;
    this.jwtSessionRepository = jwtSessionRepository;
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.objectMapper = objectMapper;
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

    // 기존에 로그인된 세션이 있으면 강제 로그아웃 처리
    jwtSessionRepository.findAllByUserIdAndRevokedFalse(user.getId())
        .forEach(JwtSession::revoke);

    String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
    String refreshToken = jwtProvider.generateRefreshToken(user.getId());

    JwtSession session = new JwtSession(
        user.getId(),
        refreshToken,
        Instant.now().plusMillis(jwtProvider.getRefreshTokenExpirationMs())
    );
    jwtSessionRepository.save(session);

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