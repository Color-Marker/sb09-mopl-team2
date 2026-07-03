package com.sb09.sb09moplteam2.auth.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.auth.service.MailService;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.user.entity.User;
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

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MailService mailService;

  @InjectMocks
  private BasicAuthService basicAuthService;

  private User createUser() {
    User user = new User("우디", "woody@mopl.io", "encodedOldPassword");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

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
}