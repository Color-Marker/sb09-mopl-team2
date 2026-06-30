package com.sb09.sb09moplteam2.follow.service;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.exception.follow.*;
import com.sb09.sb09moplteam2.follow.dto.data.FollowDto;
import com.sb09.sb09moplteam2.follow.dto.request.FollowRequest;
import com.sb09.sb09moplteam2.follow.entity.Follow;
import com.sb09.sb09moplteam2.follow.repository.FollowRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  // 1. 팔로우 하기
  @Transactional
  public FollowDto follow(UUID followerId, FollowRequest request) {
    UUID followeeId = request.getFolloweeId();

    User followee = userRepository.findById(followeeId)
        .orElseThrow(() -> new MoplException(ErrorCode.USER_NOT_FOUND));

    if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
      throw new AlreadyFollowingException();
    }

    User follower = userRepository.getReferenceById(followerId);

    Follow follow = new Follow(follower, followee);
    Follow savedFollow = followRepository.save(follow);

    return new FollowDto(
        savedFollow.getId(),
        savedFollow.getFollowee().getId(),
        savedFollow.getFollower().getId()
    );
  }

  // 2. 팔로우 취소
  @Transactional
  public void unfollow(UUID currentUserId, UUID followId) {
    Follow follow = followRepository.findById(followId)
        .orElseThrow(FollowNotFoundException::new);

    if (!follow.getFollower().getId().equals(currentUserId)) {
      throw new FollowForbiddenException();
    }

    followRepository.delete(follow);
  }

  // 3. 특정 유저를 내가 팔로우하는지 여부 조회
  public boolean isFollowing(UUID followerId, UUID followeeId) {
    return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
  }

  // 4. 특정 유저의 팔로워 수 조회
  public long getFollowerCount(UUID followeeId) {
    return followRepository.countByFolloweeId(followeeId);
  }
}
