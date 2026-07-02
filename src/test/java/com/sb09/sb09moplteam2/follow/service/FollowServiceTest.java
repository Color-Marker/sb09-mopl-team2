package com.sb09.sb09moplteam2.follow.service;

import com.sb09.sb09moplteam2.exception.follow.AlreadyFollowingException;
import com.sb09.sb09moplteam2.exception.follow.FollowForbiddenException;
import com.sb09.sb09moplteam2.follow.dto.request.FollowRequest;
import com.sb09.sb09moplteam2.follow.entity.Follow;
import com.sb09.sb09moplteam2.follow.repository.FollowRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @InjectMocks
  private FollowService followService;

  @Mock
  private FollowRepository followRepository;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("이미 팔로우 중인 유저를 다시 팔로우하면 AlreadyFollowingException이 발생한다.")
  void follow_AlreadyFollowing_ThrowsException() {
    // given
    UUID followerId = UUID.randomUUID();
    UUID followeeId = UUID.randomUUID();
    FollowRequest request = new FollowRequest(followeeId);

    // 가짜 유저(Mock) 생성
    User mockFollowee = mock(User.class);
    given(userRepository.findById(followeeId)).willReturn(Optional.of(mockFollowee));

    given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).willReturn(true);

    // when & then
    assertThatThrownBy(() -> followService.follow(followerId, request))
        .isInstanceOf(AlreadyFollowingException.class);
  }

  @Test
  @DisplayName("권한이 없는 유저가 팔로우 취소를 시도하면 FollowForbiddenException이 발생한다.")
  void unfollow_Forbidden_ThrowsException() {
    // given
    UUID currentUserId = UUID.randomUUID();
    UUID followId = UUID.randomUUID();

    // 가짜 팔로워와 팔로위 생성 후 가짜 ID 부여 (현재 로그인 유저 ID와 다르게 세팅)
    User realFollower = mock(User.class);
    given(realFollower.getId()).willReturn(UUID.randomUUID()); // currentUserId와 불일치

    User followee = mock(User.class);
    given(followee.getId()).willReturn(UUID.randomUUID());

    Follow mockFollow = new Follow(realFollower, followee);
    given(followRepository.findById(followId)).willReturn(Optional.of(mockFollow));

    // when & then
    assertThatThrownBy(() -> followService.unfollow(currentUserId, followId))
        .isInstanceOf(FollowForbiddenException.class);
  }

  @Test
  @DisplayName("팔로우 취소 로직이 정상적으로 동작한다.")
  void unfollow_Success() {
    // given
    UUID currentUserId = UUID.randomUUID();
    UUID followId = UUID.randomUUID();

    // 가짜 팔로워(나)와 팔로위 생성 후 ID 부여 (현재 로그인 유저 ID와 일치하게 세팅)
    User me = mock(User.class);
    given(me.getId()).willReturn(currentUserId); // currentUserId와 일치

    User followee = mock(User.class);
    given(followee.getId()).willReturn(UUID.randomUUID());

    Follow mockFollow = new Follow(me, followee);
    given(followRepository.findById(followId)).willReturn(Optional.of(mockFollow));

    // when
    followService.unfollow(currentUserId, followId);

    // then
    verify(followRepository).delete(mockFollow);
  }
}
