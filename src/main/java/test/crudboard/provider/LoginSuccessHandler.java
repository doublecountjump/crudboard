package test.crudboard.provider;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.service.JwtService;
import test.crudboard.service.RefreshTokenService;

import java.io.IOException;

/**
 * 0203
 * jwt 발급 후 리다이렉트시 발급한 jwt를 클라이언트가 어떻게 보관할것인가?
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("Handler Start");

        String email = null;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (LocalUserDetails) authentication.getPrincipal();
            email = userDetails.getUsername();
        }

        log.info("Generated");

        String token = jwtService.generateToken(email);
        refreshTokenService.GenerateToken(email);
        Cookie cookie = new Cookie("jwt", token);
        cookie.setSecure(true);
        cookie.setMaxAge(10); // 1시간
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect("/");
    }
}
