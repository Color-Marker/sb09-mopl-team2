package com.sb09.sb09moplteam2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.security.JwtAuthenticationFilter;
import com.sb09.sb09moplteam2.security.JwtProvider;
import com.sb09.sb09moplteam2.security.JwtSignInFilter;
import com.sb09.sb09moplteam2.security.JwtSignOutHandler;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

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
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .addLogoutHandler(new JwtSignOutHandler(jwtSessionRepository))
            .logoutSuccessHandler((request, response, authentication) ->
                response.setStatus(org.springframework.http.HttpStatus.NO_CONTENT.value()))
            .deleteCookies("REFRESH_TOKEN")
        )
        .addFilterAt(jwtSignInFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}