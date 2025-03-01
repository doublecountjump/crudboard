package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Like;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.PostDto;
import test.crudboard.entity.dto.PostResponseDto;
import test.crudboard.entity.dto.TitleDto;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.LikeRepository;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PostService{
    private final JpaUserRepository userRepository;
    private final JpaPostRepository postRepository;
    private final LikeRepository likeRepository;
    private final RedisTemplate<String, String> template;
    private static final String VIEW_COUNT_PREFIX = "view:content:";

    public Post save(PostDto postDto){
        User user = userRepository.findById(postDto.getUserid())
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        Post post = Post.builder()
                .head(postDto.getHead())
                .context(postDto.getContext())
                .build();
        post.setUser(user);

        return postRepository.save(post);
    }
    public List<TitleDto> getTitleList(){
        List<TitleDto> postList = postRepository.findPostList();

        for (TitleDto titleDto : postList) {
            String key = VIEW_COUNT_PREFIX + titleDto.getId();
            String view = template.opsForValue().get(key);

            if(view!=null){
                titleDto.setView(Long.parseLong(view));
            }
        }


        return postList;
    }

    public PostResponseDto getPostById(Long id){
        String key = VIEW_COUNT_PREFIX + id;
        Post post = postRepository.findPostByUserId(id).orElseThrow(() -> new EntityNotFoundException("entity not found"));

        if(template.hasKey(key)){
            template.opsForValue().increment(key);
        }else {
            template.opsForValue().set(key,String.valueOf(post.getView() + 1));
        }
        Long view = Long.parseLong(Objects.requireNonNull(template.opsForValue().get(key)));

        return new PostResponseDto(post, view);
    }

    public boolean isPostOwner(Long postId, String name){
        return postRepository.existsPostByIdAndUserNickname(postId, name);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
        log.info("Delete Post! id : {}", id);
    }

    public void recommendPost(Long postId, String name) {
        boolean b = likeRepository.existsLikeByPostIdAndUserNickname(postId, name);

        if(b){
            likeRepository.deleteLikeByPostIdAndUserNickname(postId,name);
        }else{

            Like like = Like.builder()
                    .user(userRepository.findUserByNickname(name).orElseThrow(() -> new EntityNotFoundException("user not found")))
                    .post(postRepository.findById(postId).orElseThrow(()-> new EntityNotFoundException("post not found")))
                    .build();

            likeRepository.save(like);
        }
    }
}
