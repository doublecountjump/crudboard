package test.crudboard.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import test.crudboard.domain.entity.token.Token;
import test.crudboard.service.JwtService;
import test.crudboard.service.RefreshTokenService;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response){

        //요청에 첨부한 리프레시토큰 조회
        Token refreshToken = refreshTokenService.getRefreshToken(request);

        //토큰의 유효기간 확인.
        if(!refreshTokenService.validRefreshToken(refreshToken)){
            throw new BadCredentialsException("토큰이 유효하지 않습니다.");
        }

        //리프레시 토큰으로 새 엑세스 토큰 생성
        String refreshAccessToken = refreshTokenService.getRefreshAccessToken(refreshToken);

        //헤더에 반환
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + refreshAccessToken)
                .build();


    }
}
