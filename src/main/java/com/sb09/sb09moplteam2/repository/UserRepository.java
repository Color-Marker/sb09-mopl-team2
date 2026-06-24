package com.sb09.sb09moplteam2.repository;

import com.sb09.sb09moplteam2.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

}
