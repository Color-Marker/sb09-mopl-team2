package com.sb09.sb09moplteam2.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

  private static final String COOKIE_NAME = "REFRESH_TOKEN";

  private final boolean secure;
  private final JwtProvider jwtProvider;

  public RefreshTokenCookieFactory(
      @Value("${mopl.cookie.secure:false}") boolean secure,
      JwtProvider jwtProvider
  ) {
    this.secure = secure;
    this.jwtProvider = jwtProvider;
  }

  public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie(COOKIE_NAME, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(secure);
    cookie.setPath("/");
    cookie.setMaxAge((int) (jwtProvider.getRefreshTokenExpirationMs() / 1000));
    cookie.setAttribute("SameSite", "Lax");
    response.addCookie(cookie);
  }
}
