package test.crudboard.service;


import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.UserInfoDto;
import test.crudboard.entity.dto.UserJoinDto;
import test.crudboard.entity.enumtype.AuthProvider;
import test.crudboard.entity.enumtype.Roles;
import test.crudboard.repository.JpaUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    private final JpaUserRepository repository;
    private final PasswordEncoder encoder;
    public User save(UserJoinDto userJoinDto){

        if(repository.findUserByEmail(userJoinDto.getEmail()).isPresent()){
            throw new DuplicateRequestException("user already exist");
        }
        User user = User.builder()
                .email(userJoinDto.getEmail())
                .password(encoder.encode(userJoinDto.getPassword()))
                .provider(AuthProvider.LOCAL)
                .roles(Roles.ROLE_USER)
                .build();

        return repository.save(user);
    }

    public User findUserById(Long id){
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    public User findUserByEmail(String email){
        return repository.findUserByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));
    }

    public boolean isIdentification(Long userId, String email) {
        return repository.existsUserByIdAndEmail(userId, email);
    }


    public UserInfoDto getUserInfo(String email){
        User user = repository.findUserByEmail(email).orElseThrow(() -> new EntityNotFoundException());

        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .githubId(user.getGithubId())
                .avatar_url(user.getProfileImage())
                .provider(user.getProvider())
                .roles(user.getRoles())
                .postList(user.getPostList())
                .commentList(user.getCommentList())
                .build();
    }

    public UserInfoDto getUserInfoById(Long id){
        User user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException());

        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .githubId(user.getGithubId())
                .avatar_url(user.getProfileImage())
                .provider(user.getProvider())
                .roles(user.getRoles())
                .postList(user.getPostList())
                .commentList(user.getCommentList())
                .build();
    }
}
