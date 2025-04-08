package test.crudboard.domain.entity.post.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.comment.dto.PostFooterDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailDto {
    private PostHeaderDto header;
    private List<Comment> commentList;
}
