package test.crudboard.security.provider.local;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalUserProvider implements AuthenticationProvider {
    private final LocalUserDetailsService service;
    private final PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("name : {}", authentication.getName());
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = service.loadUserByUsername(name);

        boolean matches = encoder.matches(password, userDetails.getPassword());

        if(matches){
            return new UsernamePasswordAuthenticationToken(userDetails,password,userDetails.getAuthorities());
        }

        throw new BadCredentialsException("bad credentials");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
