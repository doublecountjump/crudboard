package test.crudboard.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Token;
import test.crudboard.entity.User;
import test.crudboard.repository.TokenRepository;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    @Value("${jwt.secret}")
    private String secretKey;

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
            User user = token.getUser();

            return jwtService.generateToken(user.getEmail());
        }else throw new BadCredentialsException("bad");     //에러 수정하기
    }
}
