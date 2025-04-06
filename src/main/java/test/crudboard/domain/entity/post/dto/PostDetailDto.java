package test.crudboard.domain.entity.post.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import test.crudboard.domain.entity.comment.dto.PostFooterDto;

@Getter
@AllArgsConstructor
public class PostDetailDto {
    private PostHeaderDto header;
    private PostFooterDto footer;
}
