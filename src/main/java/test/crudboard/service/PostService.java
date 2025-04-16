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

    //게시글 저장
    @Transactional
    public Post save(CreatePostDto createPostDto, Long id){

        Post post = Post.builder()
                .head(createPostDto.getHead())
                .context(createPostDto.getContext())
                .view(0L)
                .like_count(0L)
                .build();

        post.setUser(User.Quick(id));
        Post save = postRepository.save(post);

        redisService.savePostHeader(save);

        return save;
    }

    /**
     * 메인화면의 타이틀 목록을 가져옴
     * @param page 가져오고자 하는 페이지
     */
    public Page<PostHeaderDto> getTitleList(Integer page, boolean isRecommend){
        Page<PostHeaderDto> dto;

        try {
            dto = redisService.getTitleList(page);
        }catch (CacheNotFoundException e){
            System.out.println(e.getMessage());
            log.error("[getTitleList] Redis Error");
            PageRequest created = PageRequest.of(page - 1, 20, Sort.by("id").descending());
            Page<Object[]> objectPage = postRepository.findPostList(created);

            List<Long> list = objectPage.getContent().stream().map(o -> (Long) o[0]).toList();
            Map<Long, Long> views = redisService.getView(list);

            List<PostHeaderDto> dtoList = getPostHeaderDtos(objectPage,views);
            dto = new PageImpl<>(
                    dtoList,
                    objectPage.getPageable(),
                    objectPage.getTotalElements()
            );


        }

        return dto;
    }

    private static List<PostHeaderDto> getPostHeaderDtos(Page<Object[]> objectPage,Map<Long, Long> views) {
        List<PostHeaderDto> dtoList = new ArrayList<>();
        for (Object[] content : objectPage.getContent()) {
            PostHeaderDto header = new PostHeaderDto();
            Long key = (Long)content[0];
            header.setPost_id(key);
            header.setHead((String) content[1]);
            header.setContext((String) content[2]);
            header.setView(
                    views.get(key) != null ? views.get(key) : (Long)content[3]
            );
            header.setCreated((LocalDateTime)content[4]);
            header.setLike_count((Long) content[5]);
            header.setComment_count((Long) content[6]);
            header.setNickname((String) content[7]);

            dtoList.add(header);
        }
        return dtoList;
    }


    /**
     * 해당하는 id의 게시글을 가져옴
     * @param postId 게시글의 id
     * @return
     */
    @Transactional
    public PostDetailDto getPostDetailDtoById(Long postId, boolean isRecommend){
        List<Comment> footer = commentService.getCommentList(postId);
        PostHeaderDto header;
        try{
            PostHeader postHeader = redisService.getPostHeader(postId);
            header = new PostHeaderDto(postHeader);
            return new PostDetailDto(header,footer);
        }
        catch (CacheNotFoundException e) {
            log.warn("[{}] {}", postId, e.getMessage());
            log.warn("[{}] 게시글이 캐시에 존재하지 않습니다.", postId);

            header = postRepository.findPostDetailDto(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
            redisService.addHeader(header);
        }


        return new PostDetailDto(header, footer);
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
        return postRepository.findMainTitleDtoByPostHead(text, created);
    }

    public Page<PostHeaderDto> searchPostByHeadOrContent(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByPostHeadOrPostContent(text, created);
    }

    public Page<PostHeaderDto> searchPostByNickname(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByUserNickname(text, created);
    }

    public Post findById(Long id){
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
    }

    private static PostHeaderDto getPostHeaderDto(Long postId, Map<String, Object> resultMap) {
        PostHeaderDto header = new PostHeaderDto();
        header.setPost_id(postId);
        header.setHead((String) resultMap.get(HEAD));
        header.setContext((String) resultMap.get(CONTEXT));
        header.setView(Long.parseLong((String)resultMap.get(VIEW)));
        header.setLike_count(Long.parseLong((String)resultMap.get(LIKE_COUNT)));
        header.setComment_count(Long.parseLong((String)resultMap.get(COMMENT_COUNT)));
        header.setNickname((String) resultMap.get(NICKNAME));
        return header;
    }
}
