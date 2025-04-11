package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.CreatePostDto;
import test.crudboard.domain.entity.post.dto.PostDetailDto;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.error.CacheNotFoundException;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;import java.util.stream.Collectors;

import static test.crudboard.domain.type.RedisDataType.POST_LIKE_COUNT;
import static test.crudboard.domain.type.RedisDataType.POST_VIEW_COUNT;
import static test.crudboard.domain.type.RedisField.*;
import static test.crudboard.domain.type.RedisKeyType.*;


/**
 * 게시글에서 가장 자주 사용되는 기능을 생성(게시글, 댓글) 그리고 게시글 조회로 가정함
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class  PostService{
    private final JpaUserRepository userRepository;
    private final JpaPostRepository postRepository;
    private final RedisService redisService;
    private final CommentService commentService;
    private final RedisTemplate<String, String> template;

    //게시글 저장
    @Transactional
    public Post save(CreatePostDto createPostDto){
        User user = userRepository.findUserByNickname(createPostDto.getName())
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        Post post = Post.builder()
                .head(createPostDto.getHead())
                .context(createPostDto.getContext())
                .view(0L)
                .build();
        post.setUser(user);
        Post save = postRepository.save(post);

        redisService.saveHeader(save);
        return save;
    }

    /**
     * 메인화면의 타이틀 목록을 가져옴
     * @param page 가져오고자 하는 페이지
     */
    public Page<PostHeaderDto> getTitleList(Integer page, boolean isRecommend){

        PageRequest created = PageRequest.of(page - 1, 20,Sort.by("post_id").descending());
        Page<PostHeaderDto> postList = postRepository.findPostList(created);


        try {
            for (PostHeaderDto dto : postList) {
                Long id = dto.getPost_id();

                Object viewObj = template.opsForHash().get(POST_VIEW_COUNT.formatKey(id), id.toString());
                Long view = (viewObj != null) ? Long.parseLong(viewObj.toString()) : 0L;

                Object likeObj = template.opsForHash().get(POST_LIKE_COUNT.formatKey(id), id.toString());
                Long like = (likeObj != null) ? Long.parseLong(likeObj.toString()) : 0L;

                dto.setView(view);
                dto.setLike_count(like);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            log.error("[getTitleList] 조회수가 캐시에 존재하지 않습니다.");
        }

        return postList;
    }

    /**
     * 해당하는 id의 게시글을 가져옴
     * @param postId 게시글의 id
     * @return
     */
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetailDtoById(Long postId, boolean isRecommend){
        List<Comment> list = commentService.getCommentList(postId);

        if(isRecommend) {
            Map<String, Object> entry = template.opsForHash().entries(HOT_POST_DATA.formatKey(postId))
                    .entrySet().stream().collect(Collectors.toMap(
                            key -> (String)key.getKey(),
                            Map.Entry::getValue
                    ));

            if (entry.isEmpty()) {
                log.warn("[{}}] 추천 게시글이 캐시에 없습니다.",postId);
                //캐시에 없을 때 처리
                PostHeaderDto postHeaderDto = postRepository.findPostDetailDto(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
                return new PostDetailDto(postHeaderDto, list);
            }

            PostHeaderDto header = getPostHeaderDto(postId, entry);
            return new PostDetailDto(header,list);
        }

        PostHeaderDto header;
        try{
            header = redisService.getHeaderDto(postId);
        }
        catch (CacheNotFoundException e) {
            log.warn("[{}] {}",postId,e.getErrorCode().getMessage());
            header = postRepository.findPostDetailDto(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        }
            template.opsForHash().increment(POST_VIEW_COUNT.formatKey(postId), postId.toString(), 1);

            Object viewObj = template.opsForHash().get(POST_VIEW_COUNT.formatKey(postId), postId.toString());
            Long view = (viewObj != null) ? Long.parseLong(viewObj.toString()) : 0L;
            header.setView(view);

            return new PostDetailDto(header, list);

    }


    public boolean isPostOwner(Long postId, String name){
        return postRepository.existsPostByIdAndUserNickname(postId, name);
    }
    @Transactional
    public Post update(Long postId, CreatePostDto postDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        post.setHead(postDto.getHead());
        post.setContext(postDto.getContext());

        return post;
    }
    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
        log.info("Delete Post! id : {}", id);
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


    private static PostHeaderDto getPostHeaderDto(Long postId, Map<String, Object> resultMap) {
        PostHeaderDto header = new PostHeaderDto();
        header.setPost_id(postId);
        header.setHead((String) resultMap.get(HEAD));
        header.setContext((String) resultMap.get(CONTEXT));
        header.setView(Long.parseLong((String)resultMap.get(VIEW)));
        header.setLike_count(Long.parseLong((String)resultMap.get(LIKES)));
        header.setComment_count(Long.parseLong((String)resultMap.get(COMMENTS)));
        header.setNickname((String) resultMap.get(NICKNAME));
        return header;
    }
}
