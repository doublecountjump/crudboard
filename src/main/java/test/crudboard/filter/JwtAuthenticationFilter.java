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
import test.crudboard.entity.enumtype.Roles;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.service.JwtService;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("filter start");
        String token = jwtService.extractToken(request);
        System.out.println(token);
        try {
            if (token != null && jwtService.validToken(token)) {
                Claims claims = jwtService.parseClaims(token);

                JwtUserDetails userDetails = new JwtUserDetails(
                        claims.get("id", Long.class),
                        Roles.valueOf(claims.get("roles", String.class)));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("Context in");
            }
            filterChain.doFilter(request,response);
        }catch (ExpiredJwtException e){
            log.info("JWT token is expired. Removing cookie...");

            // 쿠키 삭제
            Cookie cookie = new Cookie("jwt", null);
            cookie.setMaxAge(0); // 즉시 만료
            cookie.setPath("/");  // 쿠키의 경로 설정
            response.addCookie(cookie);

            // Security Context 초기화
            SecurityContextHolder.clearContext();

            response.sendRedirect("/logout");
            return;
        }

    }

}
