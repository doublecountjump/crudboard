package test.crudboard.domain.entity.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("post")
public class PostHeaderDto {
    @Id
    private Long post_id;
    private String head;
    private String context;
    private Long view;
    private LocalDateTime created;
    private Long like_count;
    private Long comment_count;

    private String nickname;


    @TimeToLive
    private Long timeout;

    public void setTimeOut(boolean b){
        this.timeout  = 3 * 24 * 60 * 60L;
    }


    public PostHeaderDto(Long post_id, String head, String context, Long view,
                         LocalDateTime created, Long like_count, Long comment_count, String nickname ){
        this.post_id = post_id;
        this.head = head;
        this.context = context;
        this.view = view;
        this.created = created;
        this.like_count = like_count;
        this.comment_count = comment_count;
        this.nickname = nickname;
    }

}