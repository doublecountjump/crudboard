package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.CreatePostDto;
import test.crudboard.domain.entity.post.dto.PostDetailDto;
import test.crudboard.domain.entity.post.dto.PostHeader;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.error.CacheNotFoundException;
import test.crudboard.repository.JpaPostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;

import static test.crudboard.domain.type.RedisField.*;


/**
 * 게시글에서 가장 자주 사용되는 기능을 생성(게시글, 댓글) 그리고 게시글 조회로 가정함
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class  PostService{
    private final JpaPostRepository postRepository;
    private final CommentService commentService;
    private final RedisService redisService;

/*    @PostConstruct
    public void init(){
        PageRequest pageRequest = PageRequest.of(0, 50000);
        List<Post> allLimit200 = postRepository.findAllLimit(pageRequest);
        for (Post post : allLimit200) {
            redisService.savePostHeader(post);
        }
    }*/

    public Post findById(Long id){
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
    }

    //게시글 저장
    @Transactional
    public Post save(CreatePostDto createPostDto, Long id){

        Post post = Post.builder()
                .head(createPostDto.getHead())
                .context(createPostDto.getContext())
                .view(0L)
                .user(User.Quick(id))
                .build();

        Post save = postRepository.save(post);

        return save;
    }

    @Transactional
    public Post update(Long postId, CreatePostDto postDto) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        String head = postDto.getHead();
        String context = postDto.getContext();

        post.setHead(head);
        post.setContext(context);

        redisService.update(postId, HEAD, head);
        redisService.update(postId, CONTEXT, context);

        return post;
    }

    @Transactional
    public void deletePost(Long id) {
        redisService.deleteCacheById(id);
        postRepository.deleteById(id);
        log.info("Delete Post! id : {}", id);
    }


    public boolean isPostOwner(Long postId, String name){
        return postRepository.existsPostByIdAndUserNickname(postId, name);
    }

    public Page<PostHeaderDto> searchPostByHead(String text, PageRequest created) {
        int page = created.getPageNumber() - 1;
        Long max = postRepository.maxPostId();

        long startPage = max - (10000L * page);
        long endPage = max - (10000L * page + 1);

        return postRepository.findMainTitleDtoByPostHead(text, created);
    }

    public Page<PostHeaderDto> searchPostByHeadOrContent(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByPostHeadOrPostContent(text, created);
    }

    public Page<PostHeaderDto> searchPostByNickname(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByUserNickname(text, created);
    }

}
