package test.crudboard.service;


import com.sun.jdi.request.DuplicateRequestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.entity.user.dto.UserInfoDto;
import test.crudboard.domain.entity.user.dto.UserJoinDto;
import test.crudboard.security.type.AuthProvider;
import test.crudboard.security.type.Roles;
import test.crudboard.repository.JpaUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    private final JpaUserRepository userRepository;
    private final PasswordEncoder encoder;

    public User save(UserJoinDto userJoinDto){
        //사용자가 존재하는지 확인
        if(userRepository.existsUserByEmail(userJoinDto.getEmail())){
            throw new DuplicateRequestException("user already exist");
        }
        //닉네임 중복 확인
        else if(userRepository.existsUserByNickname(userJoinDto.getNickname())){
            throw new DuplicateRequestException("nickname exist");
        }

        User user = User.builder()
                .email(userJoinDto.getEmail())
                .password(encoder.encode(userJoinDto.getPassword()))
                .nickname(userJoinDto.getNickname())
                .provider(AuthProvider.LOCAL)
                .roles(Roles.ROLE_USER)
                .build();

        return userRepository.save(user);
    }

    public User findUserByEmail(String email){
        return userRepository.findUserByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));
    }

    public boolean isIdentification(Long userId, String name) {
        return userRepository.existsUserByIdAndNickname(userId, name);
    }

    public UserInfoDto getUserInfo(String name){
        User user = userRepository.findUserByNickname(name).orElseThrow(EntityNotFoundException::new);
        return new UserInfoDto(user);
    }

}
