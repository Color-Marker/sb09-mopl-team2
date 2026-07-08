package com.sb09.sb09moplteam2.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;

class UserMapperTest {

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  private User createUser() {
    User user = new User("우디", "woody@mopl.io", "encodedPassword");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(user, "profileImageUrl", "/files/profile.png");
    return user;
  }

  @Test
  void toDto는_User의_모든_필드를_UserDto로_변환한다() {
    User user = createUser();

    UserDto dto = userMapper.toDto(user);

    assertThat(dto.id()).isEqualTo(user.getId());
    assertThat(dto.email()).isEqualTo("woody@mopl.io");
    assertThat(dto.name()).isEqualTo("우디");
    assertThat(dto.profileImageUrl()).isEqualTo("/files/profile.png");
    assertThat(dto.role()).isEqualTo(Role.USER);
    assertThat(dto.locked()).isFalse();
  }

  @Test
  void toSummary는_User의_id를_userId로_매핑한다() {
    User user = createUser();

    UserSummary summary = userMapper.toSummary(user);

    assertThat(summary.userId()).isEqualTo(user.getId());
    assertThat(summary.name()).isEqualTo("우디");
    assertThat(summary.profileImageUrl()).isEqualTo("/files/profile.png");
  }

  @Test
  void toDto는_profileImageUrl이_null이어도_정상_변환한다() {
    User user = new User("우디", "woody@mopl.io", "encodedPassword");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    UserDto dto = userMapper.toDto(user);

    assertThat(dto.profileImageUrl()).isNull();
    assertThat(dto.email()).isEqualTo("woody@mopl.io");
  }

  @Test
  void toDto는_role이_ADMIN인_경우_정상_변환한다() {
    User user = createUser();
    user.changeRole(Role.ADMIN);

    UserDto dto = userMapper.toDto(user);

    assertThat(dto.role()).isEqualTo(Role.ADMIN);
  }

  @Test
  void toDto는_locked가_true인_경우_정상_변환한다() {
    User user = createUser();
    user.changeLocked(true);

    UserDto dto = userMapper.toDto(user);

    assertThat(dto.locked()).isTrue();
  }
}