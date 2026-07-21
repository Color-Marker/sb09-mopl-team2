package com.sb09.sb09moplteam2.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@ExtendWith(MockitoExtension.class)
class RedisOAuth2AuthorizationRequestRepositoryTest {

  private static final String STATE = "test-state";
  private static final String KEY = "oauth2:auth-request:" + STATE;

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private RedisOAuth2AuthorizationRequestRepository repository;

  @BeforeEach
  void setUp() {
    repository = new RedisOAuth2AuthorizationRequestRepository(stringRedisTemplate);
  }

  private OAuth2AuthorizationRequest createAuthorizationRequest() {
    return OAuth2AuthorizationRequest.authorizationCode()
        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
        .clientId("test-client-id")
        .redirectUri("https://www.mopl.io/login/oauth2/code/google")
        .state(STATE)
        .build();
  }

  @Test
  @DisplayName("인증 요청을 state 키로 Redis에 TTL과 함께 저장한다")
  void saveAuthorizationRequest_state_키로_저장한다() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

    repository.saveAuthorizationRequest(
        createAuthorizationRequest(), new MockHttpServletRequest(), new MockHttpServletResponse());

    then(valueOperations).should()
        .set(eq(KEY), anyString(), eq(Duration.ofMinutes(3)));
  }

  @Test
  @DisplayName("저장한 인증 요청을 콜백의 state 파라미터로 복원한다")
  void loadAuthorizationRequest_저장한_요청을_복원한다() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

    OAuth2AuthorizationRequest original = createAuthorizationRequest();
    ArgumentCaptor<String> serializedCaptor = ArgumentCaptor.forClass(String.class);
    repository.saveAuthorizationRequest(
        original, new MockHttpServletRequest(), new MockHttpServletResponse());
    then(valueOperations).should().set(eq(KEY), serializedCaptor.capture(), any(Duration.class));

    given(valueOperations.get(KEY)).willReturn(serializedCaptor.getValue());
    MockHttpServletRequest callback = new MockHttpServletRequest();
    callback.setParameter("state", STATE);

    OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(callback);

    assertThat(loaded).isNotNull();
    assertThat(loaded.getState()).isEqualTo(original.getState());
    assertThat(loaded.getClientId()).isEqualTo(original.getClientId());
    assertThat(loaded.getAuthorizationUri()).isEqualTo(original.getAuthorizationUri());
    assertThat(loaded.getRedirectUri()).isEqualTo(original.getRedirectUri());
  }

  @Test
  @DisplayName("state 파라미터가 없으면 null을 반환하고 Redis를 조회하지 않는다")
  void loadAuthorizationRequest_state_없으면_null() {
    OAuth2AuthorizationRequest loaded =
        repository.loadAuthorizationRequest(new MockHttpServletRequest());

    assertThat(loaded).isNull();
    then(stringRedisTemplate).should(never()).opsForValue();
  }

  @Test
  @DisplayName("인증 요청 제거 시 복원된 요청을 반환하고 Redis 키를 삭제한다")
  void removeAuthorizationRequest_반환하고_삭제한다() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

    OAuth2AuthorizationRequest original = createAuthorizationRequest();
    ArgumentCaptor<String> serializedCaptor = ArgumentCaptor.forClass(String.class);
    repository.saveAuthorizationRequest(
        original, new MockHttpServletRequest(), new MockHttpServletResponse());
    then(valueOperations).should().set(eq(KEY), serializedCaptor.capture(), any(Duration.class));

    given(valueOperations.get(KEY)).willReturn(serializedCaptor.getValue());
    MockHttpServletRequest callback = new MockHttpServletRequest();
    callback.setParameter("state", STATE);

    OAuth2AuthorizationRequest removed =
        repository.removeAuthorizationRequest(callback, new MockHttpServletResponse());

    assertThat(removed).isNotNull();
    assertThat(removed.getState()).isEqualTo(STATE);
    then(stringRedisTemplate).should().delete(KEY);
  }

  @Test
  @DisplayName("저장된 요청이 없으면 제거 시 null을 반환하고 삭제하지 않는다")
  void removeAuthorizationRequest_없으면_null() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(KEY)).willReturn(null);

    MockHttpServletRequest callback = new MockHttpServletRequest();
    callback.setParameter("state", STATE);

    OAuth2AuthorizationRequest removed =
        repository.removeAuthorizationRequest(callback, new MockHttpServletResponse());

    assertThat(removed).isNull();
    then(stringRedisTemplate).should(never()).delete(anyString());
  }
}
