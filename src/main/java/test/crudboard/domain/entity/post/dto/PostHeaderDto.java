package test.crudboard.domain.entity.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostHeaderDto {
    private Long post_id;
    private String head;
    private String context;
    private Long view;
    private LocalDateTime created;
    private Long like_count;
    private Long comment_count;

    private String nickname;
}