package com.sb09.sb09moplteam2.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2SignInFailureHandlerTest {

  @Mock private AuthenticationException authenticationException;

  private OAuth2SignInFailureHandler handler;

  @BeforeEach
  void setUp() {
    handler = new OAuth2SignInFailureHandler();
    ReflectionTestUtils.setField(handler, "frontendBaseUrl", "http://localhost:3000");
  }

  @Test
  void 인증_실패_시_에러_메시지를_포함한_URL로_리다이렉트한다() throws Exception {
    given(authenticationException.getMessage()).willReturn("OAuth2 login failed");

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(request, response, authenticationException);

    assertThat(response.getRedirectedUrl()).contains("http://localhost:3000/#/sign-in");
    assertThat(response.getRedirectedUrl()).contains("error=oauth_failed");
    assertThat(response.getRedirectedUrl()).contains("error_message=");
  }

  @Test
  void 에러_메시지에_특수문자가_있으면_URL_인코딩하여_리다이렉트한다() throws Exception {
    given(authenticationException.getMessage()).willReturn("error message with spaces");

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(request, response, authenticationException);

    assertThat(response.getRedirectedUrl()).doesNotContain(" ");
    assertThat(response.getRedirectedUrl()).contains("error+message+with+spaces");
  }
}