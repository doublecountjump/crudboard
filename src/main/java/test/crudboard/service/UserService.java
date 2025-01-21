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
import test.crudboard.entity.dto.UserDto;
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
    public User save(UserDto userDto){

        if(repository.findUserByEmail(userDto.getEmail()).isPresent()){
            throw new DuplicateRequestException("user already exist");
        }
        User user = User.builder()
                .email(userDto.getEmail())
                .password(encoder.encode(userDto.getPassword()))
                .provider(AuthProvider.LOCAL)
                .roles(Roles.ROLE_USER)
                .build();

        return repository.save(user);
    }

    public User findUserByEmail(String email){
        return repository.findUserByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));
    }
}
