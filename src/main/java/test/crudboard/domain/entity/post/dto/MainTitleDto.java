package test.crudboard.domain.entity.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MainTitleDto {
    private Long id;
    private String head;
    private String name;
    private Long view;
    private LocalDateTime created;
    private Long likeCount;
    private Long commentCount;
}