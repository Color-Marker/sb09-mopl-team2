package com.sb09.sb09moplteam2.auth.repository;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JwtSessionRepository extends JpaRepository<JwtSession, UUID> {

  List<JwtSession> findAllByUserIdAndRevokedFalse(UUID userId);

  Optional<JwtSession> findByRefreshTokenAndRevokedFalse(String refreshToken);

  @Modifying
  @Query("DELETE FROM JwtSession s WHERE s.revoked = true OR s.expirationTime < :now")
  int deleteAllRevokedOrExpired(@Param("now") Instant now);
}