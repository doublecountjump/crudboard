package test.crudboard.domain.entity.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.crudboard.domain.entity.comment.Comment;

import java.util.List;


/**
 *  댓글을 저장하기 위한 dto
 */
@Getter
@Setter
public class PostFooterDto {
    private Long CommentId;
    private Long postId;
    private String content;
    private String username;
}
