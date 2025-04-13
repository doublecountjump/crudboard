package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.CreatePostDto;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisRepository redisRepository;
    private final RedisTemplate<String,String> template;
    private final JpaPostRepository postRepository;
    public void savePostHeader(Post post) {
        PostHeader dto = init(post);

        PostHeader save = redisRepository.save(dto);

        String key = getZsetKey(LocalDate.now());
        String postId = save.getPost_id().toString();
        double score = 1.0 * save.getPost_id();

        template.opsForZSet().add(key, postId, score);

        Long total = template.opsForZSet().zCard(key);
        if(total >= 100000L){
            log.warn("이번달 게시글 목록의 저장 수가 10만개를 넘어섰습니다.");
        }
    }

    private static PostHeader init(Post post) {
        PostHeader dto = new PostHeader();
        dto.setPost_id(post.getId());
        dto.setHead(post.getHead());
        dto.setContext(post.getContext());
        dto.setCreated(post.getCreated());
        dto.setView(post.getView());
        dto.setLike_count(post.getLike_count());
        dto.setComment_count(0L);
        dto.setNickname(post.getUser().getNickname());
        dto.setTtl(false);
        return dto;
    }


    public String getZsetKey(LocalDate localDate){
        String code = "post:"+localDate.getYear() + ":" +localDate.getMonthValue();
        return code;
    }

    public PostHeader getPostHeader(Long postId) {
        return redisRepository.findById(postId).orElseThrow(() -> new CacheNotFoundException(ErrorCode.NO_DATA_IN_CACHE));
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
            throw new CacheNotFoundException(ErrorCode.INSUFFICIENT_DATA_IN_CACHE);
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

    public void update(Post post) {
        String key = "post:" + post.getId();
        String head = post.getHead();
        String context = post.getContext();

        try {
            template.opsForHash().put(key, "head", head);
            template.opsForHash().put(key, "context", context);
        }catch (Exception e){
            log.warn("[{}] 캐시 저장중 문제 발생 : {}", post.getId(), e.getMessage() );
        }

    }
}