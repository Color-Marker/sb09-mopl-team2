package com.sb09.sb09moplteam2.follow.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.sb09.sb09moplteam2.exception.ErrorCode;
import com.sb09.sb09moplteam2.exception.MoplException;
import com.sb09.sb09moplteam2.exception.follow.AlreadyFollowingException;
import com.sb09.sb09moplteam2.exception.follow.SelfFollowNotAllowedException;
import com.sb09.sb09moplteam2.follow.dto.data.FollowDto;
import com.sb09.sb09moplteam2.follow.entity.Follow;
import com.sb09.sb09moplteam2.follow.repository.FollowRepository;
import com.sb09.sb09moplteam2.notification.service.NotificationService;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @InjectMocks
  private FollowService followService;

  @Mock private FollowRepository followRepository;
  @Mock private UserRepository userRepository;
  @Mock private NotificationService notificationService;
  @Mock private ApplicationEventPublisher eventPublisher;

  // 테스트에 사용할 고정 가짜 ID들
  private final UUID followerId = UUID.randomUUID();
  private final UUID followeeId = UUID.randomUUID();

  @Test
  @DisplayName("1. 정상적인 팔로우 요청 시 성공적으로 저장되고 알림/이벤트가 발송된다.")
  void follow_Success() {
    // given
    User follower = mock(User.class);
    User followee = mock(User.class);
    Follow savedFollow = mock(Follow.class);
    UUID fakeFollowId = UUID.randomUUID();

    given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
    given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).willReturn(false);
    given(userRepository.getReferenceById(followerId)).willReturn(follower);
    given(followRepository.save(any(Follow.class))).willReturn(savedFollow);

    given(savedFollow.getId()).willReturn(fakeFollowId);
    given(savedFollow.getFollower()).willReturn(follower);
    given(savedFollow.getFollowee()).willReturn(followee);
    given(follower.getId()).willReturn(followerId);
    given(followee.getId()).willReturn(followeeId);

    // when
    FollowDto result = followService.follow(followerId, followeeId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getFollowerId()).isEqualTo(followerId);
    assertThat(result.getFolloweeId()).isEqualTo(followeeId);

    verify(notificationService, times(1)).createFollowNotification(followeeId, followerId);
    verify(eventPublisher, times(1)).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("2. 자기 자신을 팔로우하려고 하면 SelfFollowNotAllowedException이 발생한다.")
  void follow_Fail_SelfFollow() {
    // given
    UUID sameId = UUID.randomUUID();

    // when & then
    assertThatThrownBy(() -> followService.follow(sameId, sameId))
        .isInstanceOf(SelfFollowNotAllowedException.class);
  }

  @Test
  @DisplayName("3. 팔로우 하려는 대상 유저가 존재하지 않으면 MoplException이 발생한다.")
  void follow_Fail_UserNotFound() {
    // given
    given(userRepository.findById(followeeId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> followService.follow(followerId, followeeId))
        .isInstanceOf(MoplException.class);
  }

  @Test
  @DisplayName("4. 이미 팔로우 중인 유저를 다시 팔로우하려고 하면 AlreadyFollowingException이 발생한다.")
  void follow_Fail_AlreadyFollowing() {
    // given
    User followee = mock(User.class);
    given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
    given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).willReturn(true);

    // when & then
    assertThatThrownBy(() -> followService.follow(followerId, followeeId))
        .isInstanceOf(AlreadyFollowingException.class);
  }
}
