package test.crudboard.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import test.crudboard.entity.Post;

@Data
@AllArgsConstructor
public class PostResponseDto {
    private Post post;
    private Long view;
}
