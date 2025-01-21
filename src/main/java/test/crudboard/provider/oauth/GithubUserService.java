package test.crudboard.provider.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import test.crudboard.entity.User;
import test.crudboard.entity.enumtype.AuthProvider;
import test.crudboard.entity.enumtype.Roles;
import test.crudboard.repository.JpaUserRepository;


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

        User result = repository.findUserByEmail(oAuth2User.getAttribute("email"))
                .map(user -> updateUser(user, oAuth2User))
                .orElseGet(() -> setUserInfo(oAuth2User));

        return oAuth2User;
    }


    private User setUserInfo(OAuth2User oAuth2User){
        Integer id = oAuth2User.getAttribute("id");          // GitHub ID
        String login = oAuth2User.getAttribute("login");    // GitHub 계정명
        String name = oAuth2User.getAttribute("name");      // 실제 이름
        String email = oAuth2User.getAttribute("email");    // 이메일
        String avatar = oAuth2User.getAttribute("avatar_url"); // 프로필 이미지

        log.info("id : {} ", id);
        log.info("login : {} ", login);
        log.info("name : {} ", name);
        log.info("email : {} ", email);
        log.info("avatar : {} ", avatar);

        User user = new User();
        user.setEmail(email);
        user.setPassword(null);
        user.setGithubId(id.toString());
        user.setProfileImage(avatar);
        user.setProvider(AuthProvider.GITHUB);
        user.setRoles(Roles.ROLE_USER);

        return repository.save(user);
    }

    private User updateUser(User user, OAuth2User oAuth2User){
        Integer id = oAuth2User.getAttribute("id");          // GitHub ID
        String avatar = oAuth2User.getAttribute("avatar_url"); // 프로필 이미지
        user.setGithubId(id.toString());

        return user;
    }
}
