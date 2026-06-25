package com.sb09.sb09moplteam2.user.service;

import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.response.UserDto;

public interface UserService {

  UserDto createUser(UserCreateRequest request);
}