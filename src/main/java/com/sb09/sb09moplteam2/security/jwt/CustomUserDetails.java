package com.sb09.sb09moplteam2.security.jwt;

import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final UUID id;
  private final String email;
  private final String password;
  private final Role role;
  private final boolean locked;

  public CustomUserDetails(UUID id, String email, String password, Role role, boolean locked) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.role = role;
    this.locked = locked;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !locked;
  }

  @Override
  public boolean isEnabled() {
    return !locked;
  }
}