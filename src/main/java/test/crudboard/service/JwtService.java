package test.crudboard.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;
import test.crudboard.entity.Token;
import test.crudboard.entity.User;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.TokenRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JwtService {
    private final UserService userService;
    private final RedisTemplate<String, String> template;

    @Value("${jwt.secret}")
    private String secretKey;

    //토큰 생성 메서드
    public String generateToken(String email){
        User user = userService.findUserByEmail(email);

        if (user == null) {
            throw new BadCredentialsException("user not found");
        }

        Claims claims = Jwts.claims();
        claims.put("id", user.getNickname());
        claims.put("roles", user.getRoles().toString());

        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSecretKey())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (60 * 1000)))
                .compact();

    }

    /**
     * 토큰이 블랙리스트인지 확인
     * @param token
     * @return true : 블랙리스트에 존재, false : 블랙리스트에 없음
     */
    private boolean isTokenBlacklisted(String token) {
       return Boolean.TRUE.equals(template.hasKey("blacklist:" + token));
    }

    //토큰 검증 메서드
    public boolean validToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);

            return true;
        }catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {     //에러 구체적으로?
            return false;
        }
    }

    public String extractToken(HttpServletRequest request){
        Cookie jwtCookie = WebUtils.getCookie(request, "jwt");
        if(jwtCookie == null){

        }
        return jwtCookie != null ? jwtCookie.getValue() : null;

    }

    /**
     * 토큰의 클레임 정보를 추출
     * @param token JWT 토큰 문자열
     * @return 토큰에 포함된 클레임 정보
     */
    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
