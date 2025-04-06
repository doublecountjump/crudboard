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
        Token refreshToken = refreshTokenService.getRefreshToken(request);

        if(!refreshTokenService.validRefreshToken(refreshToken)){
            throw new BadCredentialsException("bad"); // 고치기
        }

        String refreshAccessToken = refreshTokenService.getRefreshAccessToken(refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + refreshAccessToken)
                .build();


    }
}
