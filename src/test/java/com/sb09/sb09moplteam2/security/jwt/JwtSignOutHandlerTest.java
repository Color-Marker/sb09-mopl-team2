package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtSignOutHandlerTest {

  @Mock
  private JwtSessionRepository jwtSessionRepository;

  @InjectMocks
  private JwtSignOutHandler jwtSignOutHandler;

  @Test
  void REFRESH_TOKEN_쿠키가_있으면_해당_세션을_revoke하고_저장한다() {
    JwtSession session = new JwtSession(UUID.randomUUID(), "valid-refresh-token", Instant.now().plusSeconds(60));

    given(jwtSessionRepository.findByRefreshTokenAndRevokedFalse("valid-refresh-token"))
        .willReturn(Optional.of(session));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "valid-refresh-token"));

    jwtSignOutHandler.logout(request, new MockHttpServletResponse(), null);

    assertThat(session.isRevoked()).isTrue();
    verify(jwtSessionRepository).save(session);
  }

  @Test
  void 이미_revoke된_세션이면_저장하지_않는다() {
    given(jwtSessionRepository.findByRefreshTokenAndRevokedFalse("expired-token"))
        .willReturn(Optional.empty());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "expired-token"));

    jwtSignOutHandler.logout(request, new MockHttpServletResponse(), null);

    verify(jwtSessionRepository, never()).save(any());
  }

  @Test
  void 쿠키가_없으면_세션을_조회하지_않는다() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    jwtSignOutHandler.logout(request, new MockHttpServletResponse(), null);

    verify(jwtSessionRepository, never()).findByRefreshTokenAndRevokedFalse(any());
  }
}