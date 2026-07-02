package com.sb09.sb09moplteam2.profile.service;

import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.profile.dto.request.ProfileUpdateRequest;
import com.sb09.sb09moplteam2.profile.dto.response.ProfileResponse;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

  private final UserRepository userRepository;

  public ProfileResponse getMyProfile(UUID userId) {
    User user = getUserById(userId);
    return ProfileResponse.from(user);
  }

  @Transactional
  public void updateProfile(UUID userId, ProfileUpdateRequest request) {
    User user = getUserById(userId);
    user.updateProfile(request.name(), request.profileImageUrl());
  }

  private User getUserById(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
  }
}
