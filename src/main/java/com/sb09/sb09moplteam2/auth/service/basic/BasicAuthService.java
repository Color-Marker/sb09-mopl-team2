package com.sb09.sb09moplteam2.auth.service.basic;

import com.sb09.sb09moplteam2.auth.dto.response.TokenRefreshResult;
import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.auth.service.AuthService;
import com.sb09.sb09moplteam2.auth.service.MailService;
import com.sb09.sb09moplteam2.exception.auth.InvalidTokenException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import com.sb09.sb09moplteam2.user.dto.response.JwtDto;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
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
  private final JwtProvider jwtProvider;
  private final JwtSessionRepository jwtSessionRepository;
  private final UserMapper userMapper;
  private final SessionBlacklistService sessionBlacklistService;

  @Override
  @Transactional
  public void resetPassword(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> UserNotFoundException.withEmail(email));

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

  @Override
  @Transactional
  public TokenRefreshResult refresh(String refreshToken) {
    if (!jwtProvider.isValidRefreshToken(refreshToken)) {
      throw new InvalidTokenException();
    }

    JwtSession session = jwtSessionRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
        .orElseThrow(InvalidTokenException::new);

    // 이전 세션 blacklist 후 revoke
    sessionBlacklistService.blacklist(session.getId());
    session.revoke();
    jwtSessionRepository.save(session);

    UUID userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    if (user.isLocked()) {
      throw new InvalidTokenException();
    }

    // 새 세션 먼저 저장 → sessionId를 access token에 포함
    String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());
    JwtSession newSession = new JwtSession(
        user.getId(),
        newRefreshToken,
        Instant.now().plusMillis(jwtProvider.getRefreshTokenExpirationMs())
    );
    jwtSessionRepository.save(newSession);
    String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole(), newSession.getId());

    JwtDto jwtDto = new JwtDto(userMapper.toDto(user), newAccessToken);
    return new TokenRefreshResult(jwtDto, newRefreshToken);
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