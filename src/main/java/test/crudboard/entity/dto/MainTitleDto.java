package test.crudboard.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MainTitleDto {
    private Long id;
    private String head;
    private String name;
    private Long view;
    private LocalDateTime created;

    // 생성자를 통한 매핑
    public MainTitleDto(Long id, String head, String name, Long view, LocalDateTime created) {
        this.id = id;
        this.head = head;
        this.name = name;
        this.view = view;
        this.created = created;
    }
}