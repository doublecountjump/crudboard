package test.crudboard.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import test.crudboard.domain.comment.entity.Comment;
import java.util.List;

/**
 * 게시글의 상세 부분(본문, 댓글) 전달을 위한 dto
 */
@Getter
@AllArgsConstructor
public class PostDetailDto {
    private PostHeaderDto header;
    private List<Comment> commentList;
}