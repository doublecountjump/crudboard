package test.crudboard.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TitleDto {
    private Long id;
    private String head;
    private String email;
    private Long view;
    private LocalDateTime created;

    // 생성자를 통한 매핑
    public TitleDto(Long id, String head, String email, Long view, LocalDateTime created) {
        this.id = id;
        this.head = head;
        this.email = email;
        this.view = view;
        this.created = created;
    }
}