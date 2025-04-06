package test.crudboard.domain.entity.token;


import jakarta.persistence.*;
import lombok.*;

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


    // 만료 체크 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expired);
    }
}
