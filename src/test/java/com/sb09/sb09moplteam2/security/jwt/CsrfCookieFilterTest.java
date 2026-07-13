package com.sb09.sb09moplteam2.security.jwt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

class CsrfCookieFilterTest {

  private final CsrfCookieFilter filter = new CsrfCookieFilter();

  @Test
  void csrfToken이_있으면_getToken을_호출하고_다음_필터로_넘어간다() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    CsrfToken csrfToken = mock(CsrfToken.class);
    request.setAttribute("_csrf", csrfToken);

    filter.doFilterInternal(request, response, filterChain);

    verify(csrfToken).getToken();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void csrfToken이_없어도_다음_필터로_넘어간다() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}