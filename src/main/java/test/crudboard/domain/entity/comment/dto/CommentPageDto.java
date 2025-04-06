package test.crudboard.domain.entity.comment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageDto {
    private Long commentId;
    private String content;
    private Long parentId;
    private boolean isParent;
    private Long depth;
    private LocalDateTime createdAt;

    private Long postId;

    private Long userId;
    private String username;

}
