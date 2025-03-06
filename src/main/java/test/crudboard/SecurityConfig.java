package test.crudboard;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import test.crudboard.filter.JwtAuthenticationFilter;
import test.crudboard.provider.LoginSuccessHandler;
import test.crudboard.provider.local.LocalUserDetailsService;
import test.crudboard.provider.local.LocalUserProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final LocalUserDetailsService localUserDetailsService;
    private final LocalUserProvider provider;
    private final LoginSuccessHandler loginSuccessHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form  // 폼 로그인 설정
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                )
                .oauth2Login(oauth2 -> oauth2  // OAuth2 로그인 설정
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                )
                .logout(l -> l.addLogoutHandler((request, response, authentication) ->{
                    Cookie cookie = new Cookie("jwt", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                })
                        .logoutSuccessUrl("/"))

                .csrf(csrf -> csrf.disable())
                .userDetailsService(localUserDetailsService)
                .authenticationProvider(provider);

        return http.build();
    }

}