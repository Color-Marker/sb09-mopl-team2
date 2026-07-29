package com.sb09.sb09moplteam2.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class RefreshTokenCookieFactoryTest {

  @Test
  @DisplayName("설정한 수명과 보안 속성으로 REFRESH_TOKEN 쿠키를 생성한다")
  void addRefreshTokenCookie_설정값으로_쿠키를_생성한다() {
    RefreshTokenCookieFactory factory = new RefreshTokenCookieFactory(true, 3600);
    MockHttpServletResponse response = new MockHttpServletResponse();

    factory.addRefreshTokenCookie(response, "refresh-token-value");

    Cookie cookie = response.getCookie("REFRESH_TOKEN");
    assertThat(cookie).isNotNull();
    assertThat(cookie.getValue()).isEqualTo("refresh-token-value");
    assertThat(cookie.getMaxAge()).isEqualTo(3600);
    assertThat(cookie.isHttpOnly()).isTrue();
    assertThat(cookie.getSecure()).isTrue();
    assertThat(cookie.getPath()).isEqualTo("/");
    assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
  }

  @Test
  @DisplayName("쿠키 수명은 토큰 유효기간과 무관하게 설정값을 따른다")
  void addRefreshTokenCookie_수명은_설정값을_따른다() {
    RefreshTokenCookieFactory factory = new RefreshTokenCookieFactory(false, 1800);
    MockHttpServletResponse response = new MockHttpServletResponse();

    factory.addRefreshTokenCookie(response, "refresh-token-value");

    assertThat(response.getCookie("REFRESH_TOKEN").getMaxAge()).isEqualTo(1800);
  }

  @Test
  @DisplayName("로컬 환경에서는 Secure 속성이 비활성화된다")
  void addRefreshTokenCookie_secure_false면_비활성화된다() {
    RefreshTokenCookieFactory factory = new RefreshTokenCookieFactory(false, 3600);
    MockHttpServletResponse response = new MockHttpServletResponse();

    factory.addRefreshTokenCookie(response, "refresh-token-value");

    assertThat(response.getCookie("REFRESH_TOKEN").getSecure()).isFalse();
  }
}
