package com.sb09.sb09moplteam2.user.repository.custom;

import com.sb09.sb09moplteam2.user.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.user.entity.User;
import java.util.List;

public interface CustomUserRepository {
  List<User> searchUsers(UserSearchCondition condition);
  long countUsers(UserSearchCondition condition);
}
