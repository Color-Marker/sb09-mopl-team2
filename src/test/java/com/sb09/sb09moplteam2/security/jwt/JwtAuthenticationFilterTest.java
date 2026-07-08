package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.user.entity.Role;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtProvider jwtProvider;
  @Mock private SessionBlacklistService sessionBlacklistService;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtProvider, sessionBlacklistService);
    SecurityContextHolder.clearContext();
  }

  @Test
  void Authorization_헤더가_없으면_인증_없이_다음_필터로_넘어간다() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void 유효하지_않은_토큰이면_인증_없이_다음_필터로_넘어간다() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(jwtProvider.isValid("invalid-token")).willReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void 블랙리스트에_등록된_세션이면_401을_반환하고_필터체인을_중단한다() throws Exception {
    UUID sessionId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(jwtProvider.isValid("valid-token")).willReturn(true);
    given(jwtProvider.getSessionId("valid-token")).willReturn(sessionId);
    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(true);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void 유효한_토큰이고_블랙리스트에_없으면_SecurityContext에_인증을_설정한다() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(jwtProvider.isValid("valid-token")).willReturn(true);
    given(jwtProvider.getSessionId("valid-token")).willReturn(sessionId);
    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(false);
    given(jwtProvider.getRole("valid-token")).willReturn(Role.USER);
    given(jwtProvider.getUserId("valid-token")).willReturn(userId);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
  }

  @Test
  void sessionId가_null이면_블랙리스트_체크_없이_인증을_설정한다() throws Exception {
    UUID userId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(jwtProvider.isValid("valid-token")).willReturn(true);
    given(jwtProvider.getSessionId("valid-token")).willReturn(null);
    given(jwtProvider.getRole("valid-token")).willReturn(Role.ADMIN);
    given(jwtProvider.getUserId("valid-token")).willReturn(userId);

    filter.doFilterInternal(request, response, filterChain);

    verify(sessionBlacklistService, never()).isBlacklisted(any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }
}