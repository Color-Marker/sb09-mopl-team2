package com.sb09.sb09moplteam2.security.oauth;

import com.sb09.sb09moplteam2.user.entity.Provider;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    return resolveUser(registrationId, oAuth2User.getAttributes());
  }

  OAuth2User resolveUser(String registrationId, Map<String, Object> attributes) {
    String email;
    String name;
    String providerId;
    Provider provider;

    if ("google".equals(registrationId)) {
      provider = Provider.GOOGLE;
      providerId = String.valueOf(attributes.get("sub"));
      email = (String) attributes.get("email");
      name = (String) attributes.get("name");
    } else if ("kakao".equals(registrationId)) {
      provider = Provider.KAKAO;
      providerId = String.valueOf(attributes.get("id"));

      @SuppressWarnings("unchecked")
      Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
      name = properties != null ? (String) properties.get("nickname") : "카카오사용자";

      email = name + "_" + providerId + "@kakao.com";
    } else {
      throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    User user = userRepository.findByEmail(email)
        .orElseGet(() -> userRepository.save(new User(name, email, providerId, provider)));

    if (user.isLocked()) {
      throw new OAuth2AuthenticationException(new OAuth2Error("locked_account"), "잠긴 계정입니다.");
    }

    return new CustomOAuth2User(user.getId(), user.getRole(), attributes);
  }
}