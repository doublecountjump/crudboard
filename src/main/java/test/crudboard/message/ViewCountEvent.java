package test.crudboard.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;



/**
 * Serializable 인터페이스는 객체를 바이트 스트림으로 변환할 수 있도록 해주는 마커 인터페이스.
 * Redis와 같은 외부 시스템으로 객체를 전송하기 위해서는 객체를 바이트 형태로 직렬화해야 한다.
 * 이때 Serializable 인터페이스가 없으면 일부 직렬화 메커니즘(특히 Java 기본 직렬화)에서 문제가 발생할 수 있다.
 * 사실 이미 RedisConfig 에서 GenericJackson2JsonRedisSerializer 로 객체를 직렬화 하도록 설정했기에
 * 필수 설정까진 아니지만, 혹시 모를 에러를 위해 설정해두었다.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ViewCountEvent implements Serializable {
    private Long postId;
    private Long count = 1L;
    public ViewCountEvent(Long postId) {
        this.postId = postId;
    }
}
