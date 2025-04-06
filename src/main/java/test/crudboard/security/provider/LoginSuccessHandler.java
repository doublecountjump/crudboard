package test.crudboard.security.provider;

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
import test.crudboard.security.provider.local.LocalUserDetails;
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
        String email = null;

        // 로컬, sso 로그인에 따라 email 추출
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            LocalUserDetails userDetails = (LocalUserDetails) authentication.getPrincipal();
            email = userDetails.getUsername();
        }

        //토큰 생성 후 전달
        String token = jwtService.generateToken(email);
        refreshTokenService.GenerateToken(email);
        Cookie cookie = new Cookie("jwt", token);
        cookie.setSecure(true);
        cookie.setMaxAge(100); // 1시간
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect("/");
    }
}
