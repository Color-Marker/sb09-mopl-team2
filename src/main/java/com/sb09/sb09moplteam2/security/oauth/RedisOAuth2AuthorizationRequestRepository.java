package com.sb09.sb09moplteam2.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth2 인증 요청 상태를 서버 메모리 세션 대신 Redis에 저장하는 저장소.
 * 기본 구현(HttpSession)은 다중 인스턴스 환경에서 인가 요청과 콜백이 서로 다른
 * 인스턴스에 도달하면 authorization_request_not_found로 실패하므로,
 * state 파라미터를 키로 Redis에 저장해 어느 인스턴스든 상태를 복원할 수 있게 한다.
 */
@Component
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String KEY_PREFIX = "oauth2:auth-request:";
  private static final Duration TTL = Duration.ofMinutes(3);

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    String state = request.getParameter(OAuth2ParameterNames.STATE);
    if (!StringUtils.hasText(state)) {
      return null;
    }
    String serialized = stringRedisTemplate.opsForValue().get(KEY_PREFIX + state);
    if (serialized == null) {
      return null;
    }
    return deserialize(serialized);
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    if (authorizationRequest == null) {
      String state = request.getParameter(OAuth2ParameterNames.STATE);
      if (StringUtils.hasText(state)) {
        stringRedisTemplate.delete(KEY_PREFIX + state);
      }
      return;
    }
    stringRedisTemplate.opsForValue().set(
        KEY_PREFIX + authorizationRequest.getState(),
        serialize(authorizationRequest),
        TTL
    );
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String state = request.getParameter(OAuth2ParameterNames.STATE);
    if (!StringUtils.hasText(state)) {
      return null;
    }
    String key = KEY_PREFIX + state;
    String serialized = stringRedisTemplate.opsForValue().get(key);
    if (serialized == null) {
      return null;
    }
    stringRedisTemplate.delete(key);
    return deserialize(serialized);
  }

  private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(authorizationRequest);
      oos.flush();
      return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
    } catch (IOException e) {
      throw new IllegalStateException("OAuth2 인증 요청 직렬화에 실패했습니다.", e);
    }
  }

  private OAuth2AuthorizationRequest deserialize(String serialized) {
    byte[] bytes = Base64.getUrlDecoder().decode(serialized);
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return (OAuth2AuthorizationRequest) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("OAuth2 인증 요청 역직렬화에 실패했습니다.", e);
    }
  }
}
