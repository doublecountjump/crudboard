package test.crudboard.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutService {
    private final RedisTemplate<String, String> template;
    private final JwtService jwtService;

    /**
     * 토큰을 무효화시키는 메서드, 유효기간이 지나지 않았으면 블랙리스트 추가
     * @param request
     */
    public void invalidateToken(HttpServletRequest request){
        String token = jwtService.extractToken(request);
        if(token == null){
            return;
        }
        Claims claims = jwtService.parseClaims(token);
        Date expiration = claims.getExpiration();
        long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0) {
            template.opsForValue().set("blacklist:" + token, "logout", ttl, TimeUnit.SECONDS);
        }
    }
}
