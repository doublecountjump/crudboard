package test.crudboard.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
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
        //엑세스 토큰 추출
        String token = jwtService.extractToken(request);
        if(token == null){
            throw new BadCredentialsException("잘못된 접근");
        }

        /**
         * 해당 토큰의 유효기간을 확인해서, 아직 유효기간이 남아있다면, 그 시간만큼 캐시에서 블랙리스트로 관리
         */
        Claims claims = jwtService.parseClaims(token);
        Date expiration = claims.getExpiration();
        long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0) {
            template.opsForValue().set("blacklist:" + token, "logout", ttl, TimeUnit.SECONDS);
        }
    }
}
