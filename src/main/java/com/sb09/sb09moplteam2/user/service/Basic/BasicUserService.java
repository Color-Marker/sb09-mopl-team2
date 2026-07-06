package com.sb09.sb09moplteam2.user.service.Basic;

import com.sb09.sb09moplteam2.auth.entity.JwtSession;
import com.sb09.sb09moplteam2.auth.entity.PasswordResetToken;
import com.sb09.sb09moplteam2.auth.repository.JwtSessionRepository;
import com.sb09.sb09moplteam2.auth.repository.PasswordResetTokenRepository;
import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.event.message.RoleUpdatedEvent;
import com.sb09.sb09moplteam2.exception.user.DuplicateEmailException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.storage.FileStorageService;
import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.mapper.UserMapper;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final FileStorageService fileStorageService;
  private final JwtSessionRepository jwtSessionRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public UserDto createUser(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw DuplicateEmailException.withEmail(request.email());
    }

    String encodedPassword = passwordEncoder.encode(request.password());
    User user = new User(request.name(), request.email(), encodedPassword);
    User saved = userRepository.save(user);

    return userMapper.toDto(saved);
  }

  @Override
  public UserSummary getUserSummary(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    return userMapper.toSummary(user);
  }

  @Override
  @Transactional
  public void changePassword(UUID userId, String newPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    user.changePassword(passwordEncoder.encode(newPassword));

    passwordResetTokenRepository.findByUserIdAndUsedFalseAndExpiryDateAfter(userId, Instant.now())
        .ifPresent(PasswordResetToken::markUsed);
  }

  @Override
  public CursorResponse<UserDto> findUsers(UserSearchCondition condition) {
    List<User> users = userRepository.searchUsers(condition);

    boolean hasNext = users.size() > condition.getLimit();
    if (hasNext) {
      users = users.subList(0, condition.getLimit());
    }

    long totalCount = userRepository.countUsers(condition);

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !users.isEmpty()) {
      User last = users.get(users.size() - 1);
      nextCursor = extractCursor(last, condition.getSortBy());
      nextIdAfter = last.getId();
    }

    List<UserDto> data = users.stream()
        .map(userMapper::toDto)
        .toList();

    return new CursorResponse<>(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        condition.getSortBy(),
        condition.getSortDirection()
    );
  }

  private String extractCursor(User user, String sortBy) {
    return switch (sortBy) {
      case "email" -> user.getEmail();
      case "name" -> user.getName();
      case "role" -> user.getRole().name();
      case "isLocked" -> String.valueOf(user.isLocked());
      default -> user.getCreatedAt().toString();
    };
  }

  @Override
  public UserDto findUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public UserDto updateUser(UUID userId, UserUpdateRequest request, MultipartFile image) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    String profileImageUrl = (image != null && !image.isEmpty())
        ? fileStorageService.store(image)
        : null;

    user.updateProfile(request.name(), profileImageUrl);

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void updateRole(UUID userId, Role role) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    Role previousRole = user.getRole();

    user.changeRole(role);

    eventPublisher.publishEvent(
        new RoleUpdatedEvent(userId, previousRole, role)
    );

    log.info("userId {} 의 권한이 변경되었습니다.", userId);
    jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)
        .forEach(JwtSession::revoke);
  }

  @Override
  @Transactional
  public void updateLocked(UUID userId, boolean locked) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    user.changeLocked(locked);

    if (locked) {
      jwtSessionRepository.findAllByUserIdAndRevokedFalse(userId)
          .forEach(JwtSession::revoke);
    }
  }
}