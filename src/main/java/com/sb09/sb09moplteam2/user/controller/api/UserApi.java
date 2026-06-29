package com.sb09.sb09moplteam2.user.controller.api;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.ChangePasswordRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserLockUpdateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserRoleUpdateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "사용자 관리")
public interface UserApi {

  @Operation(summary = "사용자 등록 (회원가입)")
  ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request);

  @Operation(summary = "비밀번호 변경")
  ResponseEntity<Void> changePassword(UUID userId, @Valid @RequestBody ChangePasswordRequest request);

  @Operation(summary = "[어드민] 사용자 목록 조회 (커서 페이지네이션)")
  ResponseEntity<CursorResponse<UserDto>> findUsers(
      String emailLike,
      Role roleEqual,
      Boolean isLocked,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy
  );

  @Operation(summary = "사용자 상세 조회")
  ResponseEntity<UserDto> findUser(UUID userId);

  @Operation(summary = "프로필 변경")
  ResponseEntity<UserDto> updateUser(UUID userId, UserUpdateRequest request, MultipartFile image);

  @Operation(summary = "[어드민] 권한 수정")
  ResponseEntity<Void> updateRole(UUID userId, @Valid @RequestBody UserRoleUpdateRequest request);

  @Operation(summary = "[어드민] 계정 잠금 상태 변경")
  ResponseEntity<Void> updateLocked(UUID userId, @Valid @RequestBody UserLockUpdateRequest request);
}