package com.sb09.sb09moplteam2.config.admin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private AdminProperties adminProperties;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AdminInitializer adminInitializer;

  @Test
  void 어드민_계정이_없으면_ADMIN_권한으로_계정을_생성한다() throws Exception {
    given(adminProperties.getEmail()).willReturn("system@mopl.io");
    given(adminProperties.getUsername()).willReturn("system");
    given(adminProperties.getPassword()).willReturn("mopl1!");
    given(userRepository.existsByEmail("system@mopl.io")).willReturn(false);
    given(passwordEncoder.encode("mopl1!")).willReturn("encodedPassword");

    adminInitializer.run(null);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getEmail()).isEqualTo("system@mopl.io");
    assertThat(saved.getName()).isEqualTo("system");
    assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  void 어드민_계정이_이미_존재하면_저장하지_않는다() throws Exception {
    given(adminProperties.getEmail()).willReturn("system@mopl.io");
    given(userRepository.existsByEmail("system@mopl.io")).willReturn(true);

    adminInitializer.run(null);

    verify(userRepository, never()).save(any());
  }
}