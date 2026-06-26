package com.sb09.sb09moplteam2.profile.repository;

import com.sb09.sb09moplteam2.profile.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

  boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  long countByFollowerId(UUID followerId);

  long countByFolloweeId(UUID followeeId);

  Slice<Follow> findByFolloweeIdAndIdLessThanOrderByIdDesc(UUID followeeId, UUID cursor, Pageable pageable);

  Slice<Follow> findByFollowerIdAndIdLessThanOrderByIdDesc(UUID followerId, UUID cursor, Pageable pageable);
}
