package com.sb09.sb09moplteam2.auth.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.auth.dto.response.TokenRefreshResult;
import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.auth.service.MailService;
import com.sb09.sb09moplteam2.exception.auth.InvalidTokenException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private MailService mailService;
  @Mock private JwtProvider jwtProvider;
  @Mock private JwtSessionRepository jwtSessionRepository;
  @Mock private UserMapper userMapper;
  @Mock private SessionBlacklistService sessionBlacklistService;

  @InjectMocks
  private BasicAuthService basicAuthService;

  private User createUser() {
    User user = new User("우디", "woody@mopl.io", "encodedOldPassword");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  // ── resetPassword ──────────────────────────────────────────────

  @Test
  void 비밀번호_초기화에_성공하면_기존_토큰을_무효화하고_새_토큰을_저장한다() {
    User user = createUser();
    PasswordResetToken oldToken = new PasswordResetToken(user.getId(), "oldEncodedTemp", Instant.now().plusSeconds(60));

    given(userRepository.findByEmail("woody@mopl.io")).willReturn(Optional.of(user));
    given(passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId())).willReturn(List.of(oldToken));
    given(passwordEncoder.encode(any())).willReturn("encodedNewTemp");

    basicAuthService.resetPassword("woody@mopl.io");

    assertThat(oldToken.isUsed()).isTrue();

    ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(passwordResetTokenRepository).save(captor.capture());
    assertThat(captor.getValue().getUserId()).isEqualTo(user.getId());
    assertThat(captor.getValue().getTempPassword()).isEqualTo("encodedNewTemp");

    verify(mailService).sendTemporaryPassword(eq("woody@mopl.io"), any());
  }

  @Test
  void 존재하지_않는_이메일이면_UserNotFoundException을_던진다() {
    given(userRepository.findByEmail("none@mopl.io")).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicAuthService.resetPassword("none@mopl.io"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 발급한_임시비밀번호_평문을_그대로_암호화하고_메일로도_보낸다() {
    User user = createUser();

    given(userRepository.findByEmail("woody@mopl.io")).willReturn(Optional.of(user));
    given(passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId())).willReturn(List.of());
    given(passwordEncoder.encode(any())).willReturn("encoded");

    basicAuthService.resetPassword("woody@mopl.io");

    ArgumentCaptor<String> rawPasswordCaptor = ArgumentCaptor.forClass(String.class);
    verify(passwordEncoder).encode(rawPasswordCaptor.capture());
    verify(mailService).sendTemporaryPassword(eq("woody@mopl.io"), eq(rawPasswordCaptor.getValue()));
    assertThat(rawPasswordCaptor.getValue()).hasSize(12);
  }

  // ── refresh ────────────────────────────────────────────────────

  @Test
  void 유효하지_않은_리프레시_토큰이면_InvalidTokenException을_던진다() {
    given(jwtProvider.isValidRefreshToken("bad-token")).willReturn(false);

    assertThatThrownBy(() -> basicAuthService.refresh("bad-token"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void 리프레시_토큰이_유효하지만_세션이_없으면_InvalidTokenException을_던진다() {
    given(jwtProvider.isValidRefreshToken("valid-token")).willReturn(true);
    given(jwtSessionRepository.findByRefreshTokenAndRevokedFalse("valid-token"))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> basicAuthService.refresh("valid-token"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void 토큰_갱신에_성공하면_기존_세션을_revoke하고_새_토큰을_발급한다() {
    User user = createUser();
    UUID sessionId = UUID.randomUUID();
    JwtSession oldSession = new JwtSession(user.getId(), "old-refresh", Instant.now().plusSeconds(3600));
    ReflectionTestUtils.setField(oldSession, "id", sessionId);

    given(jwtProvider.isValidRefreshToken("old-refresh")).willReturn(true);
    given(jwtSessionRepository.findByRefreshTokenAndRevokedFalse("old-refresh"))
        .willReturn(Optional.of(oldSession));
    given(jwtProvider.getUserIdFromRefreshToken("old-refresh")).willReturn(user.getId());
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(jwtProvider.generateRefreshToken(user.getId())).willReturn("new-refresh");
    given(jwtProvider.getRefreshTokenExpirationMs()).willReturn(604_800_000L);
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(inv -> {
      JwtSession s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
      return s;
    });
    given(jwtProvider.generateAccessToken(eq(user.getId()), eq(user.getRole()), any(UUID.class)))
        .willReturn("new-access");
    UserDto userDto = new UserDto(user.getId(), null, user.getEmail(), user.getName(), null, Role.USER, false);
    given(userMapper.toDto(user)).willReturn(userDto);

    TokenRefreshResult result = basicAuthService.refresh("old-refresh");

    assertThat(oldSession.isRevoked()).isTrue();
    verify(sessionBlacklistService).blacklist(sessionId);
    assertThat(result.jwtDto().accessToken()).isEqualTo("new-access");
    assertThat(result.refreshToken()).isEqualTo("new-refresh");
  }

  @Test
  void 잠긴_계정의_리프레시_토큰이면_InvalidTokenException을_던진다() {
    User user = createUser();
    user.changeLocked(true);
    UUID sessionId = UUID.randomUUID();
    JwtSession session = new JwtSession(user.getId(), "old-refresh", Instant.now().plusSeconds(3600));
    ReflectionTestUtils.setField(session, "id", sessionId);

    given(jwtProvider.isValidRefreshToken("old-refresh")).willReturn(true);
    given(jwtSessionRepository.findByRefreshTokenAndRevokedFalse("old-refresh"))
        .willReturn(Optional.of(session));
    given(jwtProvider.getUserIdFromRefreshToken("old-refresh")).willReturn(user.getId());
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(jwtSessionRepository.save(any())).willReturn(session);

    assertThatThrownBy(() -> basicAuthService.refresh("old-refresh"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void 토큰_갱신_시_유저를_찾을_수_없으면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    JwtSession session = new JwtSession(userId, "old-refresh", Instant.now().plusSeconds(3600));
    ReflectionTestUtils.setField(session, "id", sessionId);

    given(jwtProvider.isValidRefreshToken("old-refresh")).willReturn(true);
    given(jwtSessionRepository.findByRefreshTokenAndRevokedFalse("old-refresh"))
        .willReturn(Optional.of(session));
    given(jwtProvider.getUserIdFromRefreshToken("old-refresh")).willReturn(userId);
    given(userRepository.findById(userId)).willReturn(Optional.empty());
    given(jwtSessionRepository.save(any())).willReturn(session);

    assertThatThrownBy(() -> basicAuthService.refresh("old-refresh"))
        .isInstanceOf(UserNotFoundException.class);
  }
}