package com.sb09.sb09moplteam2.security.oauth;

import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomOAuth2User implements OAuth2User {

  private final UUID userId;
  private final Role role;
  private final Map<String, Object> attributes;

  public CustomOAuth2User(UUID userId, Role role, Map<String, Object> attributes) {
    this.userId = userId;
    this.role = role;
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getName() {
    return userId.toString();
  }
}