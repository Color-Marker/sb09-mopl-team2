package com.sb09.sb09moplteam2.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sb09.sb09moplteam2.config.jwt.JwtProperties;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtProperties jwtProperties;

  public String generateAccessToken(UUID userId, Role role, UUID sessionId) {
    return generateToken(userId, role, sessionId, jwtProperties.getAccessToken().getSecret(), jwtProperties.getAccessToken().getExpirationMs());
  }

  public String generateRefreshToken(UUID userId) {
    return generateToken(userId, null, null, jwtProperties.getRefreshToken().getSecret(), jwtProperties.getRefreshToken().getExpirationMs());
  }

  public UUID getUserId(String accessToken) {
    return UUID.fromString(getClaims(accessToken, jwtProperties.getAccessToken().getSecret()).getSubject());
  }

  public Role getRole(String accessToken) {
    try {
      return Role.valueOf(getClaims(accessToken, jwtProperties.getAccessToken().getSecret()).getStringClaim("role"));
    } catch (ParseException e) {
      throw new IllegalStateException("토큰 파싱에 실패했습니다.", e);
    }
  }

  public UUID getSessionId(String accessToken) {
    try {
      String sessionId = getClaims(accessToken, jwtProperties.getAccessToken().getSecret()).getStringClaim("sessionId");
      return sessionId != null ? UUID.fromString(sessionId) : null;
    } catch (ParseException e) {
      throw new IllegalStateException("토큰 파싱에 실패했습니다.", e);
    }
  }

  public boolean isValid(String accessToken) {
    return isValidToken(accessToken, jwtProperties.getAccessToken().getSecret());
  }

  public UUID getUserIdFromRefreshToken(String refreshToken) {
    return UUID.fromString(getClaims(refreshToken, jwtProperties.getRefreshToken().getSecret()).getSubject());
  }

  public boolean isValidRefreshToken(String refreshToken) {
    return isValidToken(refreshToken, jwtProperties.getRefreshToken().getSecret());
  }

  public long getRefreshTokenExpirationMs() {
    return jwtProperties.getRefreshToken().getExpirationMs();
  }

  private String generateToken(UUID userId, Role role, UUID sessionId, String secret, long expirationMs) {
    try {
      JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
          .subject(userId.toString())
          .issueTime(Date.from(Instant.now()))
          .expirationTime(Date.from(Instant.now().plusMillis(expirationMs)));
      if (role != null) {
        claimsBuilder.claim("role", role.name());
      }
      if (sessionId != null) {
        claimsBuilder.claim("sessionId", sessionId.toString());
      }
      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build());
      signedJWT.sign(new MACSigner(secret.getBytes()));
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("토큰 생성에 실패했습니다.", e);
    }
  }

  private boolean isValidToken(String token, String secret) {
    try {
      JWTClaimsSet claims = getClaims(token, secret);
      return claims.getExpirationTime() != null && claims.getExpirationTime().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private JWTClaimsSet getClaims(String token, String secret) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      MACVerifier verifier = new MACVerifier(secret.getBytes());
      if (!signedJWT.verify(verifier)) {
        throw new IllegalStateException("토큰 서명이 유효하지 않습니다.");
      }
      return signedJWT.getJWTClaimsSet();
    } catch (ParseException | JOSEException e) {
      throw new IllegalStateException("유효하지 않은 토큰입니다.", e);
    }
  }
}