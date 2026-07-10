package com.sb09.sb09moplteam2.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.user.entity.Provider;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomOAuth2UserService customOAuth2UserService;

  private User createUser(String email) {
    User user = new User("기존유저", email, "providerId123", Provider.GOOGLE);
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  @Test
  void 구글_로그인시_기존_사용자가_있으면_새로_가입시키지_않는다() {
    User existingUser = createUser("test@gmail.com");
    given(userRepository.findByEmail("test@gmail.com")).willReturn(Optional.of(existingUser));

    Map<String, Object> attributes = Map.of(
        "sub", "1234567890",
        "email", "test@gmail.com",
        "name", "테스트유저"
    );

    OAuth2User result = customOAuth2UserService.resolveUser("google", attributes);

    assertThat(((CustomOAuth2User) result).getUserId()).isEqualTo(existingUser.getId());
    verify(userRepository, never()).save(any());
  }

  @Test
  void 구글_로그인시_신규_사용자면_가입시킨다() {
    given(userRepository.findByEmail("new@gmail.com")).willReturn(Optional.empty());
    given(userRepository.save(any(User.class))).willAnswer(invocation -> {
      User user = invocation.getArgument(0);
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      return user;
    });

    Map<String, Object> attributes = Map.of(
        "sub", "9999999999",
        "email", "new@gmail.com",
        "name", "신규유저"
    );

    customOAuth2UserService.resolveUser("google", attributes);

    verify(userRepository).save(any(User.class));
  }

  @Test
  void 카카오_로그인시_가상_이메일을_생성해서_가입시킨다() {
    given(userRepository.findByEmail("카카오유저_555@kakao.com")).willReturn(Optional.empty());
    given(userRepository.save(any(User.class))).willAnswer(invocation -> {
      User user = invocation.getArgument(0);
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      return user;
    });

    Map<String, Object> attributes = Map.of(
        "id", 555L,
        "properties", Map.of("nickname", "카카오유저")
    );

    customOAuth2UserService.resolveUser("kakao", attributes);

    verify(userRepository).findByEmail("카카오유저_555@kakao.com");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void 지원하지_않는_provider면_예외를_던진다() {
    assertThatThrownBy(() -> customOAuth2UserService.resolveUser("naver", Map.of()))
        .isInstanceOf(OAuth2AuthenticationException.class);
  }

  @Test
  void 잠긴_계정으로_구글_로그인_시도하면_OAuth2AuthenticationException을_던진다() {
    User lockedUser = new User("잠긴유저", "locked@gmail.com", "providerId123", Provider.GOOGLE);
    ReflectionTestUtils.setField(lockedUser, "id", UUID.randomUUID());
    lockedUser.changeLocked(true);

    given(userRepository.findByEmail("locked@gmail.com")).willReturn(Optional.of(lockedUser));

    Map<String, Object> attributes = Map.of(
        "sub", "1234567890",
        "email", "locked@gmail.com",
        "name", "잠긴유저"
    );

    assertThatThrownBy(() -> customOAuth2UserService.resolveUser("google", attributes))
        .isInstanceOf(OAuth2AuthenticationException.class);
  }

  @Test
  void 카카오_로그인시_properties가_null이면_이름이_카카오사용자로_설정된다() {
    given(userRepository.findByEmail(any(String.class))).willReturn(Optional.empty());
    given(userRepository.save(any(User.class))).willAnswer(invocation -> {
      User user = invocation.getArgument(0);
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      return user;
    });

    Map<String, Object> attributes = new java.util.HashMap<>();
    attributes.put("id", 999L);
    attributes.put("properties", null);

    OAuth2User result = customOAuth2UserService.resolveUser("kakao", attributes);

    assertThat(result).isNotNull();
    verify(userRepository).findByEmail("카카오사용자_999@kakao.com");
  }
}