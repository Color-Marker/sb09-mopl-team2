package com.sb09.sb09moplteam2.user.service.Basic;
/*
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.exception.user.DuplicateEmailException;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private BasicUserService basicUserService;

  private UserCreateRequest createRequest(String name, String email, String password) {
    UserCreateRequest request = new UserCreateRequest();
    ReflectionTestUtils.setField(request, "name", name);
    ReflectionTestUtils.setField(request, "email", email);
    ReflectionTestUtils.setField(request, "password", password);
    return request;
  }

  @Test
  void 회원가입에_성공하면_UserDto를_반환한다() {
    UserCreateRequest request = createRequest("우디", "woody@mopl.io", "mopl1!@#$");

    given(userRepository.existsByEmail("woody@mopl.io")).willReturn(false);
    given(passwordEncoder.encode("mopl1!@#$")).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

    UserDto expected = new UserDto(null, null, "woody@mopl.io", "우디", null, Role.USER, false);
    given(userMapper.toDto(any(User.class))).willReturn(expected);

    UserDto result = basicUserService.createUser(request);

    assertThat(result.email()).isEqualTo("woody@mopl.io");
    assertThat(result.name()).isEqualTo("우디");
    verify(passwordEncoder).encode("mopl1!@#$");
  }

  @Test
  void 이미_존재하는_이메일이면_DuplicateEmailException을_던진다() {
    UserCreateRequest request = createRequest("우디", "woody@mopl.io", "mopl1!@#$");

    given(userRepository.existsByEmail("woody@mopl.io")).willReturn(true);

    assertThatThrownBy(() -> basicUserService.createUser(request))
        .isInstanceOf(DuplicateEmailException.class);
  }
}
*/
