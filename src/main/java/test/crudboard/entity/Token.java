package test.crudboard.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@Entity
public class Token {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    private String refresh;
    private LocalDateTime expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isValid;  // 토큰 유효성 상태

    // 만료 체크 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expired);
    }
}
