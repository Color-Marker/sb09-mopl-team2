package com.sb09.sb09moplteam2.auth.repository;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtSessionRepository extends JpaRepository<JwtSession, UUID> {

  List<JwtSession> findAllByUserIdAndRevokedFalse(UUID userId);

  Optional<JwtSession> findByRefreshTokenAndRevokedFalse(String refreshToken);
}