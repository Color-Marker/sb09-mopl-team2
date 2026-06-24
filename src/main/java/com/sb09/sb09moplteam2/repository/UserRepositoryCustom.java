package com.sb09.sb09moplteam2.repository;

import com.sb09.sb09moplteam2.dto.UserSearchCondition;
import com.sb09.sb09moplteam2.entity.User;
import java.util.List;

public interface UserRepositoryCustom {
  List<User> searchUsers(UserSearchCondition condition);
  long countUsers(UserSearchCondition condition);
}
