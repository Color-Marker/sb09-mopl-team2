package com.sb09.sb09moplteam2.config.admin;

import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final AdminProperties adminProperties;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.existsByEmail(adminProperties.getEmail())) {
      return;
    }

    try {
      User admin = new User(
          adminProperties.getUsername(),
          adminProperties.getEmail(),
          passwordEncoder.encode(adminProperties.getPassword())
      );
      admin.changeRole(Role.ADMIN);
      userRepository.save(admin);
    } catch (DataIntegrityViolationException e) {
      log.info("Admin 계정 생성 완료. 이 인스턴스에선 무시합니다.");
    }
  }
}