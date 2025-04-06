package test.crudboard.domain.entity.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostHeaderDto {
    private Long post_id;
    private String head;
    private String context;
    private Long view;
    private Long like_count;

    private Long user_id;
    private String nickname;
}