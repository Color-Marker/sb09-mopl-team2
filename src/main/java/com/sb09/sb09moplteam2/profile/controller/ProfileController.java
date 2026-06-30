package com.sb09.sb09moplteam2.profile.controller;

import com.sb09.sb09moplteam2.exception.profile.UserForbiddenException;
import com.sb09.sb09moplteam2.profile.dto.request.ProfileUpdateRequest;
import com.sb09.sb09moplteam2.profile.dto.response.ProfileResponse;
import com.sb09.sb09moplteam2.profile.service.ProfileService;

import com.sb09.sb09moplteam2.security.jwt.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @GetMapping("/{userId}")
  public ResponseEntity<ProfileResponse> getUserProfile(
      @PathVariable UUID userId) {

    ProfileResponse response = profileService.getMyProfile(userId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<Void> updateUserProfile(
      @PathVariable UUID userId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody ProfileUpdateRequest request) {

    if (!userId.equals(userDetails.getId())) {
      throw new UserForbiddenException();
    }

    profileService.updateProfile(userId, request);
    return ResponseEntity.noContent().build();
  }
}
