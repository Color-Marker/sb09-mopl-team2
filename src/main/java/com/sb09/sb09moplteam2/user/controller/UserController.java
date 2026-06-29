package com.sb09.sb09moplteam2.user.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.user.controller.api.UserApi;
import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.ChangePasswordRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserLockUpdateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserRoleUpdateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
    UserDto userDto = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @Override
  @PreAuthorize("#userId.toString() == authentication.principal.toString()")
  @PatchMapping("/{userId}/password")
  public ResponseEntity<Void> changePassword(
      @PathVariable UUID userId,
      @Valid @RequestBody ChangePasswordRequest request
  ) {
    userService.changePassword(userId, request.password());
    return ResponseEntity.noContent().build();
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<CursorResponse<UserDto>> findUsers(
      @RequestParam(required = false) String emailLike,
      @RequestParam(required = false) Role roleEqual,
      @RequestParam(required = false) Boolean isLocked,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit,
      @RequestParam String sortDirection,
      @RequestParam String sortBy
  ) {
    UserSearchCondition condition = UserSearchCondition.builder()
        .emailLike(emailLike)
        .roleEqual(roleEqual)
        .isLocked(isLocked)
        .cursor(cursor)
        .idAfter(idAfter)
        .limit(limit)
        .sortDirection(sortDirection)
        .sortBy(sortBy)
        .build();

    return ResponseEntity.ok(userService.findUsers(condition));
  }

  @Override
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findUser(@PathVariable UUID userId) {
    UserDto userDto = userService.findUser(userId);
    return ResponseEntity.ok(userDto);
  }

  @Override
  @PreAuthorize("#userId.toString() == authentication.principal.toString()")
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID userId,
      @RequestPart("request") @Valid UserUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    UserDto userDto = userService.updateUser(userId, request, image);
    return ResponseEntity.ok(userDto);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{userId}/role")
  public ResponseEntity<Void> updateRole(
      @PathVariable UUID userId,
      @Valid @RequestBody UserRoleUpdateRequest request
  ) {
    userService.updateRole(userId, request.role());
    return ResponseEntity.noContent().build();
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{userId}/locked")
  public ResponseEntity<Void> updateLocked(
      @PathVariable UUID userId,
      @Valid @RequestBody UserLockUpdateRequest request
  ) {
    userService.updateLocked(userId, request.locked());
    return ResponseEntity.noContent().build();
  }
}