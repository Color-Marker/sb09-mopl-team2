package com.sb09.sb09moplteam2.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

  private static final String COOKIE_NAME = "REFRESH_TOKEN";

  private final boolean secure;
  private final int maxAgeSeconds;

  public RefreshTokenCookieFactory(
      @Value("${mopl.cookie.secure:false}") boolean secure,
      // 쿠키 수명은 토큰 유효기간과 분리해 관리한다.
      // 이 값이 미사용 상태의 로그인 유지 시간이 되며, 액세스 토큰 만료(30분)마다
      // 재발급과 함께 갱신되므로 서비스 이용 중에는 로그아웃되지 않는다.
      @Value("${mopl.cookie.refresh-token-max-age:3600}") int maxAgeSeconds
  ) {
    this.secure = secure;
    this.maxAgeSeconds = maxAgeSeconds;
  }

  public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie(COOKIE_NAME, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(secure);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    cookie.setAttribute("SameSite", "Lax");
    response.addCookie(cookie);
  }
}
