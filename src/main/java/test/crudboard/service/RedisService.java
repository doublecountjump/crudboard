package test.crudboard.service;


import com.nimbusds.jose.Header;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.PostHeader;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.error.CacheNotFoundException;
import test.crudboard.domain.error.ErrorCode;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.RedisRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static test.crudboard.domain.error.ErrorCode.*;
import static test.crudboard.domain.type.RedisField.*;


/**
 * Redis 를 사용해 데이터를 처리
 * redisRepository, redisTemplate 를 사용
 * redisRepository 에는 PostHeader 객체가 저장됨
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisRepository redisRepository;
    private final RedisTemplate<String,String> template;
    private final JpaPostRepository postRepository;

    public static final Long MINUTE = 60L;
    public static final Long HOUR = 60 * 60L;
    public static final Long DAY = 60 * 60 * 12L;


    // dto 를 받아서, 캐시에 저장
    public void addHeader(PostHeaderDto dto) {
        PostHeader header = new PostHeader(dto);
        header.setTtl(HOUR);

        PostHeader save = redisRepository.save(header);
        template.opsForHash().increment(POST + header.getPost_id(), VIEW,1);
    }

    //날짜에 해당하는 페이지 목록 키를 반환
    public String getZsetKey(LocalDate localDate){
        return POST  + localDate.getYear() + ":" +localDate.getMonthValue();
    }

    public PostHeader getPostHeader(Long postId) {
        String key = POST + postId;
        List<Object> result = template.executePipelined((RedisCallback<?>) call -> {
            StringRedisConnection con = (StringRedisConnection) call;
            con.hGetAll(key);
            con.hIncrBy(key,VIEW,1);

            return null;
        });

        Map<String, String> field = (Map<String, String>) result.get(0);

        if(field.size() < 5){
            throw new CacheNotFoundException(NO_DATA_EXISTS_IN_CACHE);
        }

        System.out.println(field.keySet());
        System.out.println(field.values());
        PostHeader dto = new PostHeader();
        dto.setPost_id(Long.parseLong(field.get("post_id")));
        dto.setHead(field.get(HEAD));
        dto.setContext(field.get(CONTEXT));
        dto.setNickname(field.get(NICKNAME));
        dto.setCreated(LocalDateTime.parse(field.get(CREATED)));
        dto.setView(Long.parseLong(field.get(VIEW)));
        dto.setComment_count(Long.parseLong(field.get(COMMENT_COUNT)));
        dto.setLike_count(Long.parseLong(field.get(LIKE_COUNT)));
        return dto;
    }


    public Page<PostHeaderDto> getTitleList(Integer page) {
        int pageSize = 20;
        long start = (long) (page - 1) * pageSize;
        long end = start + pageSize - 1;

        String key = getZsetKey(LocalDate.now());
        Long size = template.opsForZSet().zCard(key);
        List<PostHeaderDto> list = new ArrayList<>();

        if(size < pageSize){
            throw new CacheNotFoundException(INSUFFICIENT_DATA_IN_CACHE);
        }
        else {
            addPostHeaderDtos(list,key, start, end);
        }

        return new PageImpl<>(
                list,
                PageRequest.of(page - 1, pageSize), // Spring Data는 0부터 시작하는 페이지 번호 사용
                1000000
        );

    }

    private void addPostHeaderDtos(List<PostHeaderDto> list,String key, long start, long end) {
        Set<String> zkeys = template.opsForZSet().reverseRange(key, start, end);
        if (zkeys == null || zkeys.isEmpty()) {
            return;
        }

        List<Long> postIds = zkeys.stream()
                .map(Long::parseLong)
                .toList();

        List<Object> objects = template.executePipelined((RedisCallback<?>) call -> {
            StringRedisConnection conn = (StringRedisConnection) call;
            for (Long postId : postIds) {
                conn.hGetAll(POST + postId.toString());
            }

            return null;
        });

        for (Object o : objects) {
            Map<String, String> field = (Map<String, String>) o;
            System.out.println(field.keySet());
            System.out.println(field.values());
            PostHeaderDto dto = new PostHeaderDto();
            dto.setPost_id(Long.parseLong(field.get("post_id")));
            dto.setHead(field.get(HEAD));
            dto.setContext(field.get(CONTEXT));
            dto.setNickname(field.get(NICKNAME));
            dto.setCreated(LocalDateTime.parse(field.get(CREATED)));
            dto.setView(Long.parseLong(field.get(VIEW)));
            dto.setComment_count(Long.parseLong(field.get(COMMENT_COUNT)));
            dto.setLike_count(Long.parseLong(field.get(LIKE_COUNT)));

            list.add(dto);
        }
    }

    public void deleteCacheById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        LocalDateTime created = post.getCreated();
        LocalDate localDate = LocalDate.of(created.getYear(), created.getMonthValue(), created.getDayOfMonth());

        String key = getZsetKey(localDate);

        redisRepository.deleteById(id);
        template.opsForZSet().remove(key, id.toString());
    }

    public void update(Long postId, String field, String data) {
        String key = "post:" + postId;

        try {
            template.opsForHash().put(key, field, data);
        }catch (Exception e){
            log.warn("[{}] 캐시 저장중 문제 발생 : {}",key, e.getMessage());
        }

    }

    public void increment(Long postId, String field, long count) {
        String key = "post:" + postId;
        try {
            template.opsForHash().increment(key, field, count);
        }catch (Exception e){
            log.warn("[{}] 캐시 저장중 문제 발생 : {}",key, e.getMessage());
        }
    }

    public Map<Long, Long> getView(List<Long> list) {
        List<Object> keys = template.executePipelined(
                (RedisCallback<?>) call -> {
                    StringRedisConnection connection = (StringRedisConnection) call;
                    for (Long key : list) {
                        connection.hGet(POST + String.valueOf(key), VIEW);
                    }

                    return null;
                }
        );

        Map<Long, Long> collect = IntStream.range(0, list.size())
                .boxed()
                .filter(i -> keys.get(i) != null)
                .collect(Collectors.toMap(
                        list::get,
                        i -> Long.parseLong((String) keys.get(i))
                ));

        return collect;
    }
}