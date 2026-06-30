package com.sb09.sb09moplteam2.security.jwt;

import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String email = authentication.getName();
    String rawPassword = String.valueOf(authentication.getCredentials());

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

    Optional<PasswordResetToken> activeToken =
        passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(user.getId(), Instant.now());

    boolean matched = activeToken.isPresent()
        ? passwordEncoder.matches(rawPassword, activeToken.get().getTempPassword())
        : user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword());

    if (!matched) {
      throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    if (user.isLocked()) {
      throw new LockedException("잠긴 계정입니다.");
    }

    CustomUserDetails principal = new CustomUserDetails(
        user.getId(), user.getEmail(), user.getPassword(), user.getRole(), user.isLocked()
    );

    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}