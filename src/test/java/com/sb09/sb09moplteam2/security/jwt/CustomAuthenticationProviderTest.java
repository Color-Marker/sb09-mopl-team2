package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CustomAuthenticationProvider provider;

  private User createUser() {
    User user = new User("우디", "woody@mopl.io", "encodedOriginalPassword");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  @Test
  void 활성_토큰이_없으면_일반_비밀번호로_로그인된다() {
    User user = createUser();
    given(userRepository.findByEmail("woody@mopl.io")).willReturn(Optional.of(user));
    given(passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(eq(user.getId()), any()))
        .willReturn(Optional.empty());
    given(passwordEncoder.matches("rawPassword", "encodedOriginalPassword")).willReturn(true);

    Authentication result = provider.authenticate(
        new UsernamePasswordAuthenticationToken("woody@mopl.io", "rawPassword"));

    assertThat(result.isAuthenticated()).isTrue();
    assertThat(((CustomUserDetails) result.getPrincipal()).getId()).isEqualTo(user.getId());
  }

  @Test
  void 활성_토큰이_있으면_임시비밀번호로만_로그인된다() {
    User user = createUser();
    PasswordResetToken activeToken = new PasswordResetToken(user.getId(), "encodedTempPassword", Instant.now().plusSeconds(60));

    given(userRepository.findByEmail("woody@mopl.io")).willReturn(Optional.of(user));
    given(passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(eq(user.getId()), any()))
        .willReturn(Optional.of(activeToken));
    given(passwordEncoder.matches("tempRaw", "encodedTempPassword")).willReturn(true);

    Authentication result = provider.authenticate(
        new UsernamePasswordAuthenticationToken("woody@mopl.io", "tempRaw"));

    assertThat(result.isAuthenticated()).isTrue();
  }

  @Test
  void 활성_토큰이_있으면_기존_비밀번호로는_로그인이_거부된다() {
    User user = createUser();
    PasswordResetToken activeToken = new PasswordResetToken(user.getId(), "encodedTempPassword", Instant.now().plusSeconds(60));

    given(userRepository.findByEmail("woody@mopl.io")).willReturn(Optional.of(user));
    given(passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(eq(user.getId()), any()))
        .willReturn(Optional.of(activeToken));
    given(passwordEncoder.matches("originalRaw", "encodedTempPassword")).willReturn(false);

    assertThatThrownBy(() -> provider.authenticate(
        new UsernamePasswordAuthenticationToken("woody@mopl.io", "originalRaw")))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void 존재하지_않는_이메일이면_BadCredentialsException() {
    given(userRepository.findByEmail("none@mopl.io")).willReturn(Optional.empty());

    assertThatThrownBy(() -> provider.authenticate(
        new UsernamePasswordAuthenticationToken("none@mopl.io", "rawPassword")))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void 잠긴_계정이면_비밀번호가_맞아도_LockedException() {
    User user = createUser();
    user.changeLocked(true);

    given(userRepository.findByEmail("woody@mopl.io")).willReturn(Optional.of(user));
    given(passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(eq(user.getId()), any()))
        .willReturn(Optional.empty());
    given(passwordEncoder.matches("rawPassword", "encodedOriginalPassword")).willReturn(true);

    assertThatThrownBy(() -> provider.authenticate(
        new UsernamePasswordAuthenticationToken("woody@mopl.io", "rawPassword")))
        .isInstanceOf(LockedException.class);
  }

  @Test
  void supports는_UsernamePasswordAuthenticationToken만_지원한다() {
    assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
  }
}