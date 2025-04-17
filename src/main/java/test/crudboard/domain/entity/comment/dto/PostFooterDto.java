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
@AllArgsConstructor
@NoArgsConstructor
public class PostFooterDto {
    private List<Comment> commentList;
    private int startPage;
    private int endPage;
}
