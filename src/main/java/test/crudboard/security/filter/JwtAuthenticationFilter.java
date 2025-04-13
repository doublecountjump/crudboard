package test.crudboard.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import test.crudboard.domain.entity.enumtype.Roles;
import test.crudboard.domain.error.ErrorCode;
import test.crudboard.domain.error.TokenExpiredException;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.JwtService;

import java.io.IOException;

/**
 * session 방식과 비교했을때 jwt 방식의 장점은?
 */

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();

        String token = jwtService.extractToken(request);
        if(token == null){
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request,response);
            long end = System.currentTimeMillis() - start;
            log.info("[Filter] JwtAuthenticationFilter.doFilterInternal (토큰 없음) - {}ms", end);

            return;
        }

        try {
            if(jwtService.isTokenBlacklisted(token)){
                // 쿠키 삭제
                Cookie cookie = new Cookie("jwt", null);
                cookie.setMaxAge(0); // 즉시 만료
                cookie.setPath("/");  // 쿠키의 경로 설정
                response.addCookie(cookie);

                // Security Context 초기화
                SecurityContextHolder.clearContext();

                throw new TokenExpiredException(ErrorCode.JWT_TOKEN_IS_EXPIRED);

            }
            if (jwtService.validToken(token)) {
                Claims claims = jwtService.parseClaims(token);

                JwtUserDetails userDetails = new JwtUserDetails(
                        claims.get("id", Long.class),
                        claims.get("nickname", String.class),
                        Roles.valueOf(claims.get("roles", String.class)));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (TokenExpiredException e){
            log.warn("JWT token is expired. Removing cookie...");

            // 쿠키 삭제
            Cookie cookie = new Cookie("jwt", null);
            cookie.setMaxAge(0); // 즉시 만료
            cookie.setPath("/");  // 쿠키의 경로 설정
            response.addCookie(cookie);

            // Security Context 초기화
            SecurityContextHolder.clearContext();
            long end = System.currentTimeMillis() - start;
            log.info("[Filter] JwtAuthenticationFilter.doFilterInternal (토큰 만료) - {}ms", end);
        }

        filterChain.doFilter(request,response);
        long end = System.currentTimeMillis() - start;
        log.info("[Filter] JwtAuthenticationFilter.doFilterInternal - {}ms", end);


    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/favicon.ico") || path.startsWith("/static/");
    }

}
