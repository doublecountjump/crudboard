package test.crudboard.domain.post.dto;

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

    //redis 에서 전달받는 객체인 PostHeader 를 위한 생성자
    public PostHeaderDto(PostHeader dto){
        this.post_id = dto.getPost_id();
        this.head = dto.getHead();
        this.context = dto.getContext();
        this.view = dto.getView();
        this.created = dto.getCreated();
        this.like_count = dto.getLike_count();
        this.comment_count = dto.getComment_count();
        this.nickname = dto.getNickname();
    }
}