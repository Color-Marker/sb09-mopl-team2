package com.sb09.sb09moplteam2.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.RefreshTokenCookieFactory;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2SignInSuccessHandlerTest {

  @Mock private JwtProvider jwtProvider;
  @Mock private JwtSessionRepository jwtSessionRepository;
  @Mock private SessionBlacklistService sessionBlacklistService;
  @Mock private Authentication authentication;

  private OAuth2SignInSuccessHandler handler;

  @BeforeEach
  void setUp() {
    handler = new OAuth2SignInSuccessHandler(
        jwtProvider, jwtSessionRepository, sessionBlacklistService,
        new RefreshTokenCookieFactory(false, jwtProvider)
    );
    ReflectionTestUtils.setField(handler, "frontendBaseUrl", "http://localhost:3000");
  }

  @Test
  void 로그인_성공_시_기존_세션을_revoke하고_블랙리스트에_등록한다() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    CustomOAuth2User principal = new CustomOAuth2User(userId, Role.USER, Map.of());

    JwtSession oldSession = new JwtSession(userId, "old-refresh", java.time.Instant.now().plusSeconds(3600));
    ReflectionTestUtils.setField(oldSession, "id", sessionId);

    given(authentication.getPrincipal()).willReturn(principal);
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)).willReturn(List.of(oldSession));
    given(jwtProvider.generateRefreshToken(userId)).willReturn("new-refresh");
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(604_800_000L);
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(inv -> inv.getArgument(0));

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationSuccess(request, response, authentication);

    assertThat(oldSession.isRevoked()).isTrue();
    verify(sessionBlacklistService).blacklist(sessionId);
    verify(jwtSessionRepository).saveAll(List.of(oldSession));
  }

  @Test
  void 로그인_성공_시_새_세션을_저장하고_REFRESH_TOKEN_쿠키를_설정한다() throws Exception {
    UUID userId = UUID.randomUUID();
    CustomOAuth2User principal = new CustomOAuth2User(userId, Role.USER, Map.of());

    given(authentication.getPrincipal()).willReturn(principal);
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)).willReturn(List.of());
    given(jwtProvider.generateRefreshToken(userId)).willReturn("new-refresh");
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(604_800_000L);
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(inv -> inv.getArgument(0));

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationSuccess(request, response, authentication);

    assertThat(response.getCookie("REFRESH_TOKEN")).isNotNull();
    assertThat(response.getCookie("REFRESH_TOKEN").getValue()).isEqualTo("new-refresh");
    assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:3000/");
  }

  @Test
  void 기존_활성_세션이_없어도_정상_처리된다() throws Exception {
    UUID userId = UUID.randomUUID();
    CustomOAuth2User principal = new CustomOAuth2User(userId, Role.USER, Map.of());

    given(authentication.getPrincipal()).willReturn(principal);
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)).willReturn(List.of());
    given(jwtProvider.generateRefreshToken(userId)).willReturn("new-refresh");
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(604_800_000L);
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(inv -> inv.getArgument(0));

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationSuccess(request, response, authentication);

    assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:3000/");
  }
}