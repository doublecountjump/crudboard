package test.crudboard.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.error.CacheNotFoundException;
import test.crudboard.domain.error.ErrorCode;
import test.crudboard.repository.RedisRepository;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisRepository redisRepository;


    public void saveHeader(Post post) {
        PostHeaderDto dto = new PostHeaderDto();
        dto.setPost_id(post.getId());
        dto.setHead(post.getHead());
        dto.setContext(post.getContext());
        dto.setCreated(post.getCreated());
        dto.setView(post.getView());
        dto.setLike_count(post.getLike_count());
        dto.setNickname(post.getUser().getNickname());

        redisRepository.save(dto);
    }

    public PostHeaderDto getHeaderDto(Long postId) {
        return redisRepository.findById(postId).orElseThrow(() -> new CacheNotFoundException(ErrorCode.NO_DATA_IN_CACHE));
    }
}
