package com.sb09.sb09moplteam2.user.service.Basic;

import com.sb09.sb09moplteam2.exception.user.DuplicateEmailException;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.response.UserDto;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDto createUser(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw DuplicateEmailException.withEmail(request.getEmail());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    User user = new User(request.getName(), request.getEmail(), encodedPassword);
    User saved = userRepository.save(user);

    return userMapper.toDto(saved);
  }
}