package test.crudboard;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import test.crudboard.provider.local.LocalUserDetailsService;
import test.crudboard.provider.local.LocalUserProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final LocalUserDetailsService localUserDetailsService;
    private final LocalUserProvider provider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form  // 폼 로그인 설정
                        .loginPage("/login")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2  // OAuth2 로그인 설정
                        .loginPage("/login")
                )
                .csrf(csrf -> csrf.disable())
                .userDetailsService(localUserDetailsService)
                .authenticationProvider(provider);

        return http.build();
    }

}