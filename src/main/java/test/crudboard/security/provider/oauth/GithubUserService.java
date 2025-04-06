package test.crudboard.security.provider.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.entity.enumtype.AuthProvider;
import test.crudboard.domain.entity.enumtype.Roles;
import test.crudboard.repository.JpaUserRepository;

import java.net.URI;
import java.net.URISyntaxException;
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

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if(email == null){
            try {
                email = getEmailFromGithubApi(userRequest.getAccessToken().getTokenValue());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
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

        log.info("user update : {}", user.getNickname());
        Integer id = oAuth2User.getAttribute("id");          // GitHub ID
        String avatar = oAuth2User.getAttribute("avatar_url"); // 프로필 이미지
        user.setGithubId(id.toString());

        return user;
    }

    private String getEmailFromGithubApi(String accessToken) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();//동기식 요청 + 블로킹 요청
        
        URI uri = new URI("https://api.github.com/user/emails");

        RequestEntity<Void> requestEntity = RequestEntity
                .get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();

        ResponseEntity<List> response = restTemplate.exchange(          //header 도 같이 보내야 하기에 exchange 사용
               requestEntity, List.class
        );

        show_JSON(response);

        //@SuppressWarnings("unchecked")
        List<Map<String, Object>> emails = (List<Map<String, Object>>) response.getBody();

        return emails.stream()
                .filter(email -> (Boolean) email.get("primary"))
                .map(email -> (String) email.get("email"))
                .findFirst()
                .orElse(null);
    }

    private void show_JSON(ResponseEntity<List> response) {
        ObjectMapper mapper = new ObjectMapper();
        try{
            String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody());
        } catch (JsonProcessingException e) {
        }
    }
}
