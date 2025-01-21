package test.crudboard.entity.dto;

import lombok.Data;

@Data
public class TitleDto {
    private Long id;
    private String head;
    private String email;

    // 생성자를 통한 매핑
    public TitleDto(Long id, String head, String email) {
        this.id = id;
        this.head = head;
        this.email = email;
    }
}