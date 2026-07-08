package com.sb09.sb09moplteam2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.security.jwt.CsrfCookieFilter;
import com.sb09.sb09moplteam2.security.oauth.CustomOAuth2UserService;
import com.sb09.sb09moplteam2.security.jwt.JwtAuthenticationFilter;
import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.JwtSignInFilter;
import com.sb09.sb09moplteam2.security.jwt.JwtSignOutHandler;
import com.sb09.sb09moplteam2.security.oauth.OAuth2SignInFailureHandler;
import com.sb09.sb09moplteam2.security.oauth.OAuth2SignInSuccessHandler;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final JwtSessionRepository jwtSessionRepository;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final ObjectMapper objectMapper;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SignInSuccessHandler oAuth2SignInSuccessHandler;
  private final OAuth2SignInFailureHandler oAuth2SignInFailureHandler;

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName("XSRF-TOKEN");
    csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");

    JwtSignInFilter jwtSignInFilter = new JwtSignInFilter(
        authenticationManager, jwtProvider, jwtSessionRepository, userRepository, userMapper, objectMapper
    );

    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            .ignoringRequestMatchers(request -> {
              String authHeader = request.getHeader("Authorization");
              return authHeader != null && authHeader.startsWith("Bearer ");
            })
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exception -> exception
            .defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                (RequestMatcher) request -> request.getRequestURI().startsWith(request.getContextPath() + "/api/")
            )
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index.html", "/favicon.svg", "/assets/**", "/error").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .addLogoutHandler(new JwtSignOutHandler(jwtSessionRepository))
            .logoutSuccessHandler((request, response, authentication) ->
                response.setStatus(HttpStatus.NO_CONTENT.value()))
            .deleteCookies("REFRESH_TOKEN")
        )
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(info -> info.userService(customOAuth2UserService))
            .successHandler(oAuth2SignInSuccessHandler)
            .failureHandler(oAuth2SignInFailureHandler)
        )
        .addFilterAt(jwtSignInFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
        .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}