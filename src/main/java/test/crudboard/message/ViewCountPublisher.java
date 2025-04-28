package test.crudboard.message;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountPublisher {
    private final RedisTemplate<String,Object> template;
    private final String VIEW_COUNT_CHANNEL =  "view_count_channel";

    public void pubViewCountEvent(Long postId){
        try{
            template.convertAndSend(VIEW_COUNT_CHANNEL, postId);
            log.info("[{}] created view event", postId);
        }catch (Exception e){
            log.error(e.getMessage());
            log.error("[{}] failed view event", postId);
        }
    }
}
