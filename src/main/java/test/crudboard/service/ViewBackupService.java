package test.crudboard.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.repository.BackUpRepository;
import test.crudboard.repository.JpaPostRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static test.crudboard.domain.type.RedisField.POST;
import static test.crudboard.domain.type.RedisField.VIEW;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewBackupService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JpaPostRepository postRepository;
    private final BackUpRepository backUpRepository;

    @Scheduled(fixedRate = 60 * 1000) // 5분마다 실행
    public void backupViewCounts() {
        // SCAN 명령어를 사용하여 키를 점진적으로 가져옴
        ScanOptions options = ScanOptions.scanOptions()
                .match(POST + "*")
                .count(100) // 한 번에 반환할 키 수 힌트
                .build();

        Cursor<String> cursor = redisTemplate.scan(options);
        Map<Long,Long> views = new HashMap<>();

        try {
            while (cursor.hasNext()) {
                String key = cursor.next();
                try {
                    if (key.matches(POST + "\\d+$")) {
                        String postIdStr = key.substring(POST.length());
                        Long postId = Long.parseLong(postIdStr);
                        Object viewCount = redisTemplate.opsForHash().get(key, VIEW);
                        String view;
                        if (viewCount instanceof Integer) {
                            view = String.valueOf(viewCount);
                        } else {
                            view = (String) viewCount;
                        }
                        if (view != null) {
                            views.put(postId, Long.parseLong(view));

                            if(views.size() >= 500){
                                backUpRepository.jdbcBatchUpdate(views);
                                views.clear();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to backup view count for key: {}", key, e);
                }
            }

            // 남은 포스트 처리
            if (!views.isEmpty()) {
                backUpRepository.jdbcBatchUpdate(views);
                log.info("Backed up view counts for final batch of {} posts", views.size());
            }
        } finally {
            cursor.close(); // 리소스 해제 필수
        }
    }
}

