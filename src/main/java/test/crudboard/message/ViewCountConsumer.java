package test.crudboard.message;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static test.crudboard.domain.type.RedisField.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class ViewCountConsumer implements MessageListener {

    private final RedisTemplate<String, Object> template;

    /**
     * Publisher가 event를 발생시키면 onMessage가 이를 확인하고 메서드를 실행시킨다.
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try{
            Integer event = (Integer) template.getValueSerializer().deserialize(message.getBody());

            if (event != null) {
                Long id = event.longValue();
                processViewCountEvent(id);  // 이벤트 처리
            }
        } catch (Exception e) {
            log.error("Error processing view count event: {}", e.getMessage(), e);
        }
    }

    /**
     * 이벤트가 발생하면 해당 Post의 조회수를 증가시킨다
     * @param id
     */
    private void processViewCountEvent(Long id) {
        try {
            String key = POST + id;  // Redis 키 생성 (예: "post:123")
            // Redis Hash 구조에서 view 필드 값을 증가
            template.opsForHash().increment(key, VIEW, 1);
        } catch (Exception e) {
            log.error("Failed to process view count for post: {}", id, e);
        }
    }
}

