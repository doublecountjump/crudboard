package test.crudboard.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Token;
import test.crudboard.entity.User;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.TokenRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtService {
    private final UserService userService;

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String email){
        User user = userService.findUserByEmail(email);

        if (user == null) {
            throw new BadCredentialsException("user not found");
        }

        Claims claims = Jwts.claims();
        claims.put("id", user.getId());
        claims.put("roles", user.getRoles().toString());

        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSecretKey())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (60 * 1000)))
                .compact();

    }

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
        String header = request.getHeader("Authorization");

        if(header != null && header.startsWith("Bearer ")){
            return header.substring(7);
        }
        return null;
    }

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
