package com.sb09.sb09moplteam2.user.service;

import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import java.util.UUID;

public interface UserService {

  UserDto createUser(UserCreateRequest request);

  UserSummary getUserSummary(UUID userId);

  void changePassword(UUID userId, String newPassword);
}