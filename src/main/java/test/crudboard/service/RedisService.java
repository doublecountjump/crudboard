package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.PostHeader;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.error.CacheNotFoundException;
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
import static test.crudboard.domain.type.RedisField.POST;
import static test.crudboard.domain.type.RedisField.VIEW;


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

    public void savePostHeader(Post post) {
        //post 를 토대로 postHeader 생성
        PostHeader dto = init(post);

        PostHeader save = redisRepository.save(dto);

        String key = getZsetKey(LocalDate.now());
        String postId = save.getPost_id().toString();
        Long score = save.getPost_id();

        //캐시에 저장된 게시글 목록에 저장
        template.opsForZSet().add(key, postId, score);

        //이번달 저장된 게시글 개수
        Long total = template.opsForZSet().zCard(key);
        if(total >= 100000L){
            log.warn("이번달 게시글 목록의 저장 수가 10만개를 넘어섰습니다.");
        }
    }

    //postHeader 생성 메서드
    private static PostHeader init(Post post) {
        PostHeader dto = new PostHeader();
        dto.setPost_id(post.getId());
        dto.setHead(post.getHead());
        dto.setContext(post.getContext());
        dto.setCreated(post.getCreated());
        dto.setView(post.getView());
        dto.setLike_count(0L);
        dto.setComment_count(0L);
        dto.setNickname(post.getUser().getNickname());
        dto.setTtl(DAY);
        return dto;
    }

    // dto 를 받아서, 캐시에 저장
    public void addHeader(PostHeaderDto dto) {
        PostHeader header = new PostHeader(dto);
        header.setTtl(HOUR);

        PostHeader save = redisRepository.save(header);
        template.opsForHash().increment("post:" + save.getPost_id(), VIEW,1);
    }


    //날짜에 해당하는 페이지 목록 키를 반환
    public String getZsetKey(LocalDate localDate){
        return "post:" + localDate.getYear() + ":" +localDate.getMonthValue();
    }

    public PostHeader getPostHeader(Long postId) {
        PostHeader header = redisRepository.findById(postId).orElseThrow(() -> new CacheNotFoundException(NO_DATA_EXISTS_IN_CACHE));
        template.opsForHash().increment("post:" + header.getPost_id(), VIEW, 1);
        return header;
    }


    public Page<PostHeaderDto> getTitleList(Integer page) {
        int pageSize = 20;
        long start = (long) (page - 1) * pageSize;
        long end = start + pageSize - 1;

        String key = getZsetKey(LocalDate.now());
        Long size = template.opsForZSet().zCard(key);
        List<PostHeaderDto> list = new ArrayList<>();

        if(size < pageSize){
            addPostHeaderDtos(list, key, 0, size);

            String newKey = getZsetKey(LocalDate.now().minusMonths(1L));

            addPostHeaderDtos(list,newKey,0,end - size);
        }
        else {
            addPostHeaderDtos(list,key, start, end);
        }

        if(list.size() < pageSize){
            throw new CacheNotFoundException(INSUFFICIENT_DATA_IN_CACHE);
        }


        return new PageImpl<>(
                list,
                PageRequest.of(page - 1, pageSize), // Spring Data는 0부터 시작하는 페이지 번호 사용
                postRepository.count()
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

        Iterable<PostHeader> allById = redisRepository.findAllById(postIds);

        for (PostHeader postHeader : allById) {
            list.add(new PostHeaderDto(postHeader));
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