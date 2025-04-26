package test.crudboard.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;

/**
 * redis 에 게시글 정보를 저장하기 위한 객체
 */
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

    //ttl(캐시 생존 시간)설정 위한 변수
    @TimeToLive
    private Long ttl;

    public PostHeader(PostHeaderDto dto){
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