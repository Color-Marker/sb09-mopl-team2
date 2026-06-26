package com.sb09.sb09moplteam2.user.controller;

import com.sb09.sb09moplteam2.user.controller.api.UserApi;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.ChangePasswordRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}