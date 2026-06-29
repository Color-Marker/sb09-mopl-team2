package com.sb09.sb09moplteam2.user.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto createUser(UserCreateRequest request);

  UserSummary getUserSummary(UUID userId);

  void changePassword(UUID userId, String newPassword);

  CursorResponse<UserDto> findUsers(UserSearchCondition condition);

  UserDto findUser(UUID userId);

  UserDto updateUser(UUID userId, UserUpdateRequest request, MultipartFile image);

  void updateRole(UUID userId, Role role);

  void updateLocked(UUID userId, boolean locked);
}