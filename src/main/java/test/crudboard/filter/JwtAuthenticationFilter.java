package test.crudboard.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import test.crudboard.entity.enumtype.Roles;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.service.JwtService;

import java.io.IOException;
import java.util.Arrays;

/**
 * session 방식과 비교했을때 jwt 방식의 장점은?
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtService.extractToken(request);
        if(token == null){
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request,response);
            return;
        }
        try {
            if (jwtService.validToken(token)) {
                Claims claims = jwtService.parseClaims(token);

                JwtUserDetails userDetails = new JwtUserDetails(
                        claims.get("id", String.class),
                        Roles.valueOf(claims.get("roles", String.class)));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());


                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (ExpiredJwtException e){
            log.warn("JWT token is expired. Removing cookie...");

            // 쿠키 삭제
            Cookie cookie = new Cookie("jwt", null);
            cookie.setMaxAge(0); // 즉시 만료
            cookie.setPath("/");  // 쿠키의 경로 설정
            response.addCookie(cookie);

            // Security Context 초기화
            SecurityContextHolder.clearContext();
        }finally {
            filterChain.doFilter(request,response);
        }

    }

}
