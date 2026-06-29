package com.sb09.sb09moplteam2.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SignInFailureHandler implements AuthenticationFailureHandler {

  @Value("${mopl.frontend.base-url}")
  private String frontendBaseUrl;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException {
    String message = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
    response.sendRedirect(frontendBaseUrl + "/#/sign-in?error=oauth_failed&error_message=" + message);
  }
}