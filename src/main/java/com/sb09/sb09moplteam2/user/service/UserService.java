package com.sb09.sb09moplteam2.user.service;

import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;


public interface UserService {

  UserDto createUser(UserCreateRequest request);
}