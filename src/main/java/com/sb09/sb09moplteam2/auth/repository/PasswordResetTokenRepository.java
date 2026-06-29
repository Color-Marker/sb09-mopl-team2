package com.sb09.sb09moplteam2.auth.repository;

import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  List<PasswordResetToken> findAllByUserIdAndUsedFalse(UUID userId);

  Optional<PasswordResetToken> findByUserIdAndUsedFalseAndExpiryDateAfter(UUID userId, Instant now);
}