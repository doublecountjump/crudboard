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
public class PostHeader {
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
    private Long ttl;

    public void setTtl(boolean recommend){
        this.ttl = recommend ?  (60 * 60 * 24 * 7L) : (60 * 60 * 24L);
    }
}
