package com.sb09.sb09moplteam2.auth.service.basic;

import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.auth.service.AuthService;
import com.sb09.sb09moplteam2.auth.service.MailService;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private static final int TEMP_PASSWORD_LENGTH = 12;
  private static final String TEMP_PASSWORD_CHARS =
      "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
  private static final Duration TEMP_PASSWORD_TTL = Duration.ofMinutes(3);

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

  @Override
  @Transactional
  public void resetPassword(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> UserNotFoundException.withEmail(email));

    // 기존 미사용 토큰은 무효화 (최신 1개만 유효하게)
    passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId())
        .forEach(PasswordResetToken::markUsed);

    String temporaryPassword = generateTemporaryPassword();
    PasswordResetToken token = new PasswordResetToken(
        user.getId(),
        passwordEncoder.encode(temporaryPassword),
        Instant.now().plus(TEMP_PASSWORD_TTL)
    );
    passwordResetTokenRepository.save(token);

    mailService.sendTemporaryPassword(user.getEmail(), temporaryPassword);
  }

  private String generateTemporaryPassword() {
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
    for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
      sb.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
    }
    return sb.toString();
  }
}