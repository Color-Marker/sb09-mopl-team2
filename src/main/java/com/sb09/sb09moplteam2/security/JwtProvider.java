package com.sb09.sb09moplteam2.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sb09.sb09moplteam2.config.JwtProperties;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//현재는 인증 필터에 필요한 액세스 토큰 처리만
@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtProperties jwtProperties;

  public String generateAccessToken(UUID userId, Role role) {
    try {
      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(userId.toString())
          .claim("role", role.name())
          .issueTime(Date.from(Instant.now()))
          .expirationTime(Date.from(Instant.now().plusMillis(jwtProperties.getAccessToken().getExpirationMs())))
          .build();

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      signedJWT.sign(new MACSigner(jwtProperties.getAccessToken().getSecret().getBytes()));
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("토큰 생성에 실패했습니다.", e);
    }
  }

  public UUID getUserId(String token) {
    return UUID.fromString(getClaims(token).getSubject());
  }

  public Role getRole(String token) {
    try {
      return Role.valueOf(getClaims(token).getStringClaim("role"));
    } catch (ParseException e) {
      throw new IllegalStateException("토큰 파싱에 실패했습니다.", e);
    }
  }

  public boolean isValid(String token) {
    try {
      JWTClaimsSet claims = getClaims(token);
      return claims.getExpirationTime() != null && claims.getExpirationTime().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private JWTClaimsSet getClaims(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      MACVerifier verifier = new MACVerifier(jwtProperties.getAccessToken().getSecret().getBytes());
      if (!signedJWT.verify(verifier)) {
        throw new IllegalStateException("토큰 서명이 유효하지 않습니다.");
      }
      return signedJWT.getJWTClaimsSet();
    } catch (ParseException | JOSEException e) {
      throw new IllegalStateException("유효하지 않은 토큰입니다.", e);
    }
  }
}