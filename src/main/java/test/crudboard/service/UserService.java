package test.crudboard.service;


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
        //사용자가 존재하는지 확인
        if(repository.findUserByEmail(userJoinDto.getEmail()).isPresent()){
            throw new DuplicateRequestException("user already exist");
        }//닉네임 중복 확인
        else if(repository.findUserByNickname(userJoinDto.getNickname()).isPresent()){
            throw new DuplicateRequestException("nickname exist");
        }
        User user = User.builder()
                .email(userJoinDto.getEmail())
                .password(encoder.encode(userJoinDto.getPassword()))
                .nickname(userJoinDto.getNickname())
                .provider(AuthProvider.LOCAL)
                .roles(Roles.ROLE_USER)
                .build();

        return repository.save(user);
    }

    public User findUserByNickname(String name){
        return repository.findUserByNickname(name).orElseThrow(()->new EntityNotFoundException("user not found"));
    }
    public User findUserByEmail(String email){
        return repository.findUserByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));
    }

    public boolean isIdentification(Long userId, String name) {
        return repository.existsUserByIdAndNickname(userId, name);
    }


    public UserInfoDto getUserInfo(String name){
        User user = repository.findUserByNickname(name).orElseThrow(() -> new EntityNotFoundException());

        System.out.println("service");
        return UserInfoDto.builder()
                .id(user.getId())
                .username(user.getNickname())
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
