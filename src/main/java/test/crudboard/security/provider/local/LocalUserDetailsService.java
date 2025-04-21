package test.crudboard.security.provider.local;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import test.crudboard.domain.entity.user.User;
import test.crudboard.repository.JpaUserRepository;

import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class LocalUserDetailsService implements UserDetailsService {
    private final JpaUserRepository repository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> result = repository.findUserByEmail(username); //email!!

        if(result.isEmpty()){
            throw new UsernameNotFoundException("user not found!!");
        }
        return new LocalUserDetails(result.get());
    }
}
