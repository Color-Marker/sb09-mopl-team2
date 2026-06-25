package com.sb09.sb09moplteam2.user.repository;

import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.Custom.CustomUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>, CustomUserRepository {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

}
