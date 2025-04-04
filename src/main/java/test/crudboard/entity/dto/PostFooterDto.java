package test.crudboard.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.crudboard.entity.Comment;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostFooterDto {
    private List<Comment> commentList;
    private int startPage;
    private int endPage;
}
