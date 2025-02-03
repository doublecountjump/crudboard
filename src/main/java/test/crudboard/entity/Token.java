package test.crudboard.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    private String refresh;
    private LocalDateTime expired;
    private String email;

    private boolean isValid;  // 토큰 유효성 상태

    // 만료 체크 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expired);
    }
}
