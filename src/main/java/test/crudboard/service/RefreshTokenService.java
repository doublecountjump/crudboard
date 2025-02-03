package test.crudboard.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Token;
import test.crudboard.entity.User;
import test.crudboard.repository.TokenRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefreshTokenService {
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.secret}")
    private String secretKey;

    public Token GenerateToken(String email){
        Optional<Token> tokenByEmail = tokenRepository.findTokenByEmail(email);
        if(tokenByEmail.isEmpty()) {
            return saveToken(email);
        }else {
            Token token = tokenByEmail.get();
            if(token.isValid()){
                return token;
            }else {
                 tokenRepository.delete(token);
                 return saveToken(email);
            }
        }
    }

    public Token getRefreshToken(HttpServletRequest request){
        String token = jwtService.extractToken(request);
        return tokenRepository.findTokenByRefresh(token).orElseThrow(() -> new JwtException("Token not Found"));
    }

    public Boolean validRefreshToken(Token token){
        LocalDateTime expired = token.getExpired();
        return expired.isBefore(LocalDateTime.now());
    }

    public String getRefreshAccessToken(Token token){

        if(validRefreshToken(token)){
            return jwtService.generateToken(token.getEmail());
        }else throw new BadCredentialsException("bad");     //에러 수정하기
    }

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Token saveToken(String email){
        Date date = new Date(System.currentTimeMillis() + (1000 * 600));
        String key = Jwts.builder()
                .setExpiration(date)
                .signWith(getSecretKey())
                .compact();
        log.info("token : {}", key);
        Token token = Token.builder()
                .refresh(key)
                .expired(LocalDateTime.now().plusMinutes(10))
                .isValid(true)
                .email(email)
                .build();
        return tokenRepository.save(token);
    }
}
