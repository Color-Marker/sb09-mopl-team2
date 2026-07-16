package com.sb09.sb09moplteam2.user.service.Basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.exception.user.DuplicateEmailException;
import com.sb09.sb09moplteam2.storage.FileStorageService;
import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private JwtSessionRepository jwtSessionRepository;

  @Mock
  private FileStorageService fileStorageService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private SessionBlacklistService sessionBlacklistService;

  @InjectMocks
  private BasicUserService basicUserService;

  private UserCreateRequest createRequest(String name, String email, String password) {
    return new UserCreateRequest(name, email, password);
  }

  private User createUser() {
    User user = new User("우디", "woody@mopl.io", "encodedOriginalPassword");
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
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
  void 사용자_목록_조회시_limit가_1미만이면_MoplException을_던진다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(0)
        .sortBy("createdAt")
        .sortDirection("DESCENDING")
        .build();

    assertThatThrownBy(() -> basicUserService.findUsers(condition))
        .isInstanceOf(MoplException.class);
    verify(userRepository, never()).searchUsers(any());
  }

  @Test
  void 이미_존재하는_이메일이면_DuplicateEmailException을_던진다() {
    UserCreateRequest request = createRequest("우디", "woody@mopl.io", "mopl1!@#$");

    given(userRepository.existsByEmail("woody@mopl.io")).willReturn(true);

    assertThatThrownBy(() -> basicUserService.createUser(request))
        .isInstanceOf(DuplicateEmailException.class);
  }

  @Test
  void getUserSummary는_UserMapper의_toSummary_결과를_그대로_반환한다() {
    User user = createUser();
    UserSummary summary = new UserSummary(user.getId(), "우디", null);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(userMapper.toSummary(user)).willReturn(summary);

    UserSummary result = basicUserService.getUserSummary(user.getId());

    assertThat(result).isEqualTo(summary);
  }

  @Test
  void changePassword는_비밀번호를_암호화해서_변경하고_활성_초기화토큰을_파기한다() {
    User user = createUser();
    PasswordResetToken activeToken = new PasswordResetToken(user.getId(), "encodedTemp", Instant.now().plusSeconds(60));

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(passwordEncoder.encode("newPassword123")).willReturn("encodedNewPassword");
    given(passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(eq(user.getId()), any()))
        .willReturn(Optional.of(activeToken));

    basicUserService.changePassword(user.getId(), "newPassword123");

    assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    assertThat(activeToken.isUsed()).isTrue();
  }

  @Test
  void updateRole는_권한을_변경하고_기존_세션을_전부_revoke한다() {
    User user = createUser();
    JwtSession session1 = new JwtSession(user.getId(), "refresh1", Instant.now().plusSeconds(60));
    JwtSession session2 = new JwtSession(user.getId(), "refresh2", Instant.now().plusSeconds(60));

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(user.getId()))
        .willReturn(List.of(session1, session2));

    basicUserService.updateRole(user.getId(), Role.ADMIN);

    assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    assertThat(session1.isRevoked()).isTrue();
    assertThat(session2.isRevoked()).isTrue();
  }

  @Test
  void updateLocked_true이면_잠금처리하고_기존_세션을_전부_revoke한다() {
    User user = createUser();
    JwtSession session = new JwtSession(user.getId(), "refresh1", Instant.now().plusSeconds(60));

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(user.getId()))
        .willReturn(List.of(session));

    basicUserService.updateLocked(user.getId(), true);

    assertThat(user.isLocked()).isTrue();
    assertThat(session.isRevoked()).isTrue();
  }

  @Test
  void updateLocked_false이면_세션을_조회하지_않는다() {
    User user = createUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    basicUserService.updateLocked(user.getId(), false);

    assertThat(user.isLocked()).isFalse();
    verify(jwtSessionRepository, never()).findAllByUserIdAndRevokedFalse(any());
  }

  @Test
  void updateUser는_이미지가_있으면_FileStorageService로_저장하고_프로필을_갱신한다() {
    User user = createUser();
    MultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", new byte[]{1, 2, 3});

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(fileStorageService.store(image)).willReturn("/files/profile.png");
    given(userMapper.toDto(user)).willReturn(
        new UserDto(user.getId(), null, user.getEmail(), "새이름", "/files/profile.png", Role.USER, false));

    UserUpdateRequest request = new UserUpdateRequest("새이름");
    UserDto result = basicUserService.updateUser(user.getId(), request, image);

    assertThat(user.getName()).isEqualTo("새이름");
    assertThat(user.getProfileImageUrl()).isEqualTo("/files/profile.png");
    assertThat(result.profileImageUrl()).isEqualTo("/files/profile.png");
  }

  @Test
  void updateUser는_이미지가_없으면_FileStorageService를_호출하지_않는다() {
    User user = createUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(
        new UserDto(user.getId(), null, user.getEmail(), "새이름", null, Role.USER, false));

    UserUpdateRequest request = new UserUpdateRequest("새이름");
    basicUserService.updateUser(user.getId(), request, null);

    assertThat(user.getName()).isEqualTo("새이름");
    verify(fileStorageService, never()).store(any());
  }

  @Test
  void findUsers는_limit보다_많이_조회되면_hasNext가_true이고_nextCursor를_채운다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(2)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    User user1 = createUser();
    User user2 = createUser();
    User user3 = createUser(); // limit(2)보다 1개 더 (hasNext 판단용)

    given(userRepository.searchUsers(condition)).willReturn(List.of(user1, user2, user3));
    given(userRepository.countUsers(condition)).willReturn(10L);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), null, u.getEmail(), u.getName(), null, Role.USER, false);
    });

    CursorResponse<UserDto> result = basicUserService.findUsers(condition);

    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo(user2.getName());
    assertThat(result.nextIdAfter()).isEqualTo(user2.getId());
    assertThat(result.totalCount()).isEqualTo(10L);
  }

  @Test
  void findUsers는_limit_이하로_조회되면_hasNext가_false이고_nextCursor는_null이다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(5)
        .sortBy("name")
        .sortDirection("ASCENDING")
        .build();

    User user1 = createUser();

    given(userRepository.searchUsers(condition)).willReturn(List.of(user1));
    given(userRepository.countUsers(condition)).willReturn(1L);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), null, u.getEmail(), u.getName(), null, Role.USER, false);
    });

    CursorResponse<UserDto> result = basicUserService.findUsers(condition);

    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextIdAfter()).isNull();
  }

  @Test
  void getUserSummary는_존재하지_않는_userId이면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicUserService.getUserSummary(userId))
        .isInstanceOf(com.sb09.sb09moplteam2.exception.user.UserNotFoundException.class);
  }

  @Test
  void changePassword는_존재하지_않는_userId이면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicUserService.changePassword(userId, "newPassword123"))
        .isInstanceOf(com.sb09.sb09moplteam2.exception.user.UserNotFoundException.class);
  }

  @Test
  void findUser는_userId로_UserDto를_반환한다() {
    User user = createUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(
        new UserDto(user.getId(), null, user.getEmail(), user.getName(), null, Role.USER, false));

    UserDto result = basicUserService.findUser(user.getId());

    assertThat(result.id()).isEqualTo(user.getId());
    assertThat(result.email()).isEqualTo(user.getEmail());
  }

  @Test
  void findUser는_존재하지_않는_userId이면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicUserService.findUser(userId))
        .isInstanceOf(com.sb09.sb09moplteam2.exception.user.UserNotFoundException.class);
  }

  @Test
  void updateRole은_존재하지_않는_userId이면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicUserService.updateRole(userId, Role.ADMIN))
        .isInstanceOf(com.sb09.sb09moplteam2.exception.user.UserNotFoundException.class);
  }

  @Test
  void updateRole은_세션_blacklist를_등록한다() {
    User user = createUser();
    JwtSession session = new JwtSession(user.getId(), "refresh1", Instant.now().plusSeconds(60));
    UUID sessionId = UUID.randomUUID();
    ReflectionTestUtils.setField(session, "id", sessionId);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(user.getId()))
        .willReturn(List.of(session));

    basicUserService.updateRole(user.getId(), Role.ADMIN);

    verify(sessionBlacklistService).blacklist(sessionId);
  }

  @Test
  void updateLocked_true이면_blacklist를_등록한다() {
    User user = createUser();
    JwtSession session = new JwtSession(user.getId(), "refresh1", Instant.now().plusSeconds(60));
    UUID sessionId = UUID.randomUUID();
    ReflectionTestUtils.setField(session, "id", sessionId);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(jwtSessionRepository.findAllByUserIdAndRevokedFalse(user.getId()))
        .willReturn(List.of(session));

    basicUserService.updateLocked(user.getId(), true);

    verify(sessionBlacklistService).blacklist(sessionId);
  }

  @Test
  void updateLocked는_존재하지_않는_userId이면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicUserService.updateLocked(userId, true))
        .isInstanceOf(com.sb09.sb09moplteam2.exception.user.UserNotFoundException.class);
  }

  @Test
  void updateUser는_존재하지_않는_userId이면_UserNotFoundException을_던진다() {
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> basicUserService.updateUser(userId, new UserUpdateRequest("새이름"), null))
        .isInstanceOf(com.sb09.sb09moplteam2.exception.user.UserNotFoundException.class);
  }

  @Test
  void findUsers는_sortBy_email이면_nextCursor가_이메일이다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(1)
        .sortBy("email")
        .sortDirection("ASCENDING")
        .build();

    User user1 = createUser();
    User user2 = createUser();

    given(userRepository.searchUsers(condition)).willReturn(List.of(user1, user2));
    given(userRepository.countUsers(condition)).willReturn(2L);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), null, u.getEmail(), u.getName(), null, Role.USER, false);
    });

    CursorResponse<UserDto> result = basicUserService.findUsers(condition);

    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo(user1.getEmail());
  }

  @Test
  void findUsers는_sortBy_role이면_nextCursor가_role_이름이다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(1)
        .sortBy("role")
        .sortDirection("ASCENDING")
        .build();

    User user1 = createUser();
    User user2 = createUser();

    given(userRepository.searchUsers(condition)).willReturn(List.of(user1, user2));
    given(userRepository.countUsers(condition)).willReturn(2L);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), null, u.getEmail(), u.getName(), null, Role.USER, false);
    });

    CursorResponse<UserDto> result = basicUserService.findUsers(condition);

    assertThat(result.nextCursor()).isEqualTo(Role.USER.name());
  }

  @Test
  void findUsers는_sortBy_isLocked이면_nextCursor가_locked_값이다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(1)
        .sortBy("isLocked")
        .sortDirection("ASCENDING")
        .build();

    User user1 = createUser();
    User user2 = createUser();

    given(userRepository.searchUsers(condition)).willReturn(List.of(user1, user2));
    given(userRepository.countUsers(condition)).willReturn(2L);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), null, u.getEmail(), u.getName(), null, Role.USER, false);
    });

    CursorResponse<UserDto> result = basicUserService.findUsers(condition);

    assertThat(result.nextCursor()).isEqualTo("false");
  }

  @Test
  void findUsers는_sortBy_createdAt이면_nextCursor가_createdAt_문자열이다() {
    UserSearchCondition condition = UserSearchCondition.builder()
        .limit(1)
        .sortBy("createdAt")
        .sortDirection("ASCENDING")
        .build();

    User user1 = createUser();
    User user2 = createUser();
    Instant now = Instant.now();
    ReflectionTestUtils.setField(user1, "createdAt", now);

    given(userRepository.searchUsers(condition)).willReturn(List.of(user1, user2));
    given(userRepository.countUsers(condition)).willReturn(2L);
    given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0);
      return new UserDto(u.getId(), null, u.getEmail(), u.getName(), null, Role.USER, false);
    });

    CursorResponse<UserDto> result = basicUserService.findUsers(condition);

    assertThat(result.nextCursor()).isEqualTo(now.toString());
  }
}