package com.sb09.sb09moplteam2.follow.repository;

import com.sb09.sb09moplteam2.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

  // 1. 특정 유저가 다른 유저를 이미 팔로우했는지 확인 (중복 체크 및 단건 조회용)
  boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  // 2. 팔로우 취소를 위해 데이터 찾기
  Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  // 3. 특정 유저의 팔로워 수 카운트
  long countByFolloweeId(UUID followeeId);

}
