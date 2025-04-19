package test.crudboard.monitoring;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisPortMonitor {
  /*  private final LettuceConnectionFactory factory;

    @Scheduled(fixedRate = 1000) // 30초마다 실행
    public void reportPoolStatus() {
        // Lettuce 연결 풀 가져오기
        try {
            // 리플렉션을 통해 내부 연결 풀에 접근
            Object pool = getConnectionPool(factory);

            if (pool instanceof GenericObjectPool) {
                GenericObjectPool<?> genericPool = (GenericObjectPool<?>) pool;

                log.warn("Redis Connection Pool Status:");
                log.warn("Active connections: {}", genericPool.getNumActive());
                log.warn("Idle connections: {}", genericPool.getNumIdle());
                log.warn("Waiters: {}", genericPool.getNumWaiters());
                log.warn("Total connections: {}", genericPool.getNumActive() + genericPool.getNumIdle());
                log.warn("Max connections: {}", genericPool.getMaxTotal());
            }
        } catch (Exception e) {
            log.error("Failed to get Redis connection pool metrics", e);
        }
    }

    private Object getConnectionPool(LettuceConnectionFactory factory) throws Exception {
        // 리플렉션을 사용하여 내부 풀 객체에 접근
        java.lang.reflect.Field poolField = factory.getClass().getDeclaredField("pool");
        poolField.setAccessible(true);
        Object pool = poolField.get(factory);

        if (pool == null) {
            return null;
        }

        // 내부 구현이 중첩된 경우가 있으므로 한 단계 더 깊이 접근
        try {
            java.lang.reflect.Field innerPoolField = pool.getClass().getDeclaredField("internalPool");
            innerPoolField.setAccessible(true);
            return innerPoolField.get(pool);
        } catch (NoSuchFieldException e) {
            // internalPool 필드가 없으면 바로 반환
            return pool;
        }
    }*/
}
