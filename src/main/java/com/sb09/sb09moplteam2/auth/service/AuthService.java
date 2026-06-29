package com.sb09.sb09moplteam2.auth.service;

import com.sb09.sb09moplteam2.auth.dto.response.TokenRefreshResult;

public interface AuthService {

  void resetPassword(String email);

  TokenRefreshResult refresh(String refreshToken);
}