package com.sb09.sb09moplteam2.user.controller.api;

import com.sb09.sb09moplteam2.user.dto.request.UserCreateRequest;
import com.sb09.sb09moplteam2.user.dto.response.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "사용자 관리")
public interface UserApi {

  @Operation(summary = "사용자 등록 (회원가입)")
  ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request);
}