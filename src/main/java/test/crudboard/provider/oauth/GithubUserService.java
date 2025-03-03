package test.crudboard.provider.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import test.crudboard.entity.User;
import test.crudboard.entity.enumtype.AuthProvider;
import test.crudboard.entity.enumtype.Roles;
import test.crudboard.repository.JpaUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GithubUserService extends DefaultOAuth2UserService {

    private final JpaUserRepository repository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("Access Token : "+ userRequest.getAccessToken());
        log.info("Provider : "+ userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("=== OAuth2User Attributes ===");
        oAuth2User.getAttributes().forEach((key, value) -> {
            log.info("{} : {}", key, value);
        });
        log.info("==========================");

        String email = (String) oAuth2User.getAttribute("email");
        if(email == null){
            email = getEmailFromGithubApi(userRequest.getAccessToken().getTokenValue());
        }


        Optional<User> result = repository.findUserByEmail(email);
        if (result.isPresent()){
            updateUser(result.get(),oAuth2User);
        }else setUserInfo(oAuth2User, userRequest, email);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", email);  // 새로 받아온 email을 attributes에 추가

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email"
        );
    }


    private User setUserInfo(OAuth2User oAuth2User, OAuth2UserRequest userRequest, String email){
        Integer id = oAuth2User.getAttribute("id");          // GitHub ID
        String login = oAuth2User.getAttribute("login");    // GitHub 계정명
        String name = oAuth2User.getAttribute("login");      // 실제 이름
        String avatar = oAuth2User.getAttribute("avatar_url"); // 프로필 이미지
        log.info("id : {} ", id);
        log.info("login : {} ", login);
        log.info("name : {} ", name);
        log.info("email : {} ", email);
        log.info("avatar : {} ", avatar);

        User user = new User();
        user.setEmail(email);
        user.setNickname(name);
        user.setPassword(null);
        user.setGithubId(id.toString());
        user.setProfileImage(avatar);
        user.setProvider(AuthProvider.GITHUB);
        user.setRoles(Roles.ROLE_USER);

        return repository.save(user);
    }

    private User updateUser(User user, OAuth2User oAuth2User){

        log.info("update!!");
        Integer id = oAuth2User.getAttribute("id");          // GitHub ID
        String avatar = oAuth2User.getAttribute("avatar_url"); // 프로필 이미지
        user.setGithubId(id.toString());

        return user;
    }

    private String getEmailFromGithubApi(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody().stream()
                .filter(email -> (Boolean) email.get("primary"))
                .map(email -> (String) email.get("email"))
                .findFirst()
                .orElse(null);
    }
}
