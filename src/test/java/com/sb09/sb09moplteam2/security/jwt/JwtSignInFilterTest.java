package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtSignInFilterTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtProvider jwtProvider;
  @Mock private JwtSessionRepository jwtSessionRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private SessionBlacklistService sessionBlacklistService;

  private JwtSignInFilter filter;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    filter = new JwtSignInFilter(
        authenticationManager, jwtProvider, jwtSessionRepository,
        userRepository, userMapper, objectMapper, sessionBlacklistService,
        new RefreshTokenCookieFactory(false, jwtProvider)
    );
  }

  @Test
  void successfulAuthentication_기존세션_revoke하고_새토큰으로_JSON응답() throws Exception {
    User user = new User("우디", "woody@mopl.io", "encodedPw");
    UUID userId = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", userId);

    CustomUserDetails principal = new CustomUserDetails(userId, "woody@mopl.io", "encodedPw", Role.USER, false);
    Authentication auth = mock(Authentication.class);
    given(auth.getPrincipal()).willReturn(principal);

    JwtSession existingSession = new JwtSession(userId, "oldRefresh", java.time.Instant.now().plusSeconds(60));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)).willReturn(List.of(existingSession));
    given(jwtProvider.generateRefreshToken(userId)).willReturn("newRefreshToken");
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(86400000L);
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(inv -> inv.getArgument(0));
    given(jwtProvider.generateAccessToken(any(UUID.class), any(Role.class), isNull())).willReturn("newAccessToken");
    given(userMapper.toDto(user)).willReturn(new UserDto(userId, null, "woody@mopl.io", "우디", null, Role.USER, false));

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    ReflectionTestUtils.invokeMethod(filter, "successfulAuthentication", request, response, chain, auth);

    assertThat(existingSession.isRevoked()).isTrue();
    verify(sessionBlacklistService).blacklist(existingSession.getId());
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentType()).contains("application/json");
    String body = response.getContentAsString();
    assertThat(body).contains("newAccessToken");
  }

  @Test
  void successfulAuthentication_기존세션없으면_blacklist_호출안함() throws Exception {
    User user = new User("우디", "woody@mopl.io", "encodedPw");
    UUID userId = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", userId);

    CustomUserDetails principal = new CustomUserDetails(userId, "woody@mopl.io", "encodedPw", Role.USER, false);
    Authentication auth = mock(Authentication.class);
    given(auth.getPrincipal()).willReturn(principal);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)).willReturn(List.of());
    given(jwtProvider.generateRefreshToken(userId)).willReturn("refreshToken");
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(86400000L);
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(inv -> inv.getArgument(0));
    given(jwtProvider.generateAccessToken(any(UUID.class), any(Role.class), isNull())).willReturn("accessToken");
    given(userMapper.toDto(user)).willReturn(new UserDto(userId, null, "woody@mopl.io", "우디", null, Role.USER, false));

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    ReflectionTestUtils.invokeMethod(filter, "successfulAuthentication", request, response, chain, auth);

    verify(sessionBlacklistService, never()).blacklist(any());
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void unsuccessfulAuthentication_401과_JSON_에러_응답() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    BadCredentialsException exception = new BadCredentialsException("bad credentials");

    ReflectionTestUtils.invokeMethod(filter, "unsuccessfulAuthentication", request, response, exception);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).contains("application/json");
    String body = response.getContentAsString();
    assertThat(body).contains("이메일 또는 비밀번호가 올바르지 않습니다.");
  }

  @Test
  void unsuccessfulAuthentication_잠긴계정이면_잠금_안내_메시지를_반환한다() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    org.springframework.security.authentication.LockedException exception =
        new org.springframework.security.authentication.LockedException("잠긴 계정입니다.");

    ReflectionTestUtils.invokeMethod(filter, "unsuccessfulAuthentication", request, response, exception);

    assertThat(response.getStatus()).isEqualTo(401);
    String body = response.getContentAsString();
    assertThat(body).contains("잠긴 계정입니다");
    assertThat(body).contains("LockedException");
  }
}