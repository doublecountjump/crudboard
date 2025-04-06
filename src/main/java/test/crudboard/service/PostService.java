package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.comment.dto.PostFooterDto;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.CreatePostDto;
import test.crudboard.domain.entity.post.dto.MainTitleDto;
import test.crudboard.domain.entity.post.dto.PostDetailDto;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.entity.user.User;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        try {
            String dataKey = POST_DATA.formatKey(save.getId());
            template.opsForHash().put(dataKey,HEAD, save.getHead());
            template.opsForHash().put(dataKey,CONTEXT, save.getContext());
            template.opsForHash().put(dataKey,NICKNAME, user.getNickname());
            template.opsForHash().put(dataKey,USER_ID,String.valueOf(user.getId()));
            template.expire(dataKey, 1, TimeUnit.HOURS);

            String statsKey = POST_STATS.formatKey(save.getId());
            template.opsForHash().put(statsKey,VIEW,String.valueOf(0L));
            template.opsForHash().put(statsKey,LIKES,String.valueOf(0L));
            template.expire(statsKey, 1, TimeUnit.HOURS);

        }catch (Exception e){
            System.out.println(e.getMessage());
            log.error("Redis Save Error");
        }

        return save;
    }

    public Post findById(Long postId){
        return postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("not found!!"));
    }

    /**
     * 메인화면의 타이틀 목록을 가져옴
     * @param page 가져오고자 하는 페이지
     */
    public Page<MainTitleDto> getTitleList(Integer page){
        PageRequest created = PageRequest.of(page - 1, 20, Sort.by("created").descending());

        Page<MainTitleDto> postList = postRepository.findPostList(created);
        try {
            for (MainTitleDto mainTitleDto : postList) {
                String key = POST_STATS.formatKey(mainTitleDto.getId());
                Object v = template.opsForHash().get(key, VIEW);
                if(v == null){
                    continue;
                }
                Long view =  Long.parseLong((String)v);
                mainTitleDto.setView(view);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            log.error("[getTitleList] Redis Error");
        }

        return postList;
    }

    /**
     * 해당하는 id의 게시글을 가져옴
     * @param postId 게시글의 id
     * @return
     */
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetailDtoById(Long postId, Integer page){

        ScanOptions scanOptions = ScanOptions.scanOptions().match(POST_ALL.formatKey(postId)).build();
        Cursor<String> scanResult = template.scan(scanOptions);

        List<String> keys = new ArrayList<>();
        while(scanResult.hasNext()){
            keys.add(scanResult.next());
        }

        if (keys.isEmpty()){
            System.out.println("cache miss");
            //캐시에 없을 때 처리
            PostHeaderDto postHeaderDto = postRepository.findPostDetailDto(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
            PostFooterDto postFooterDto = commentService.getCommentListDto(postId, page);
            putRedis10Minute(postHeaderDto);

            return new PostDetailDto(postHeaderDto, postFooterDto);
        }
        System.out.println("cache hit");
        Map<String, Object> resultMap = new HashMap<>();
        for(String key : keys){
            Map<Object, Object> entries = template.opsForHash().entries(key);
            entries.forEach((k, v) -> resultMap.put(k.toString(), v));
        }

        PostFooterDto postFooterDto = commentService.getCommentListDto(postId, page);


        PostHeaderDto header = new PostHeaderDto();
        header.setPost_id(postId);
        header.setHead((String) resultMap.get(HEAD));
        header.setContext((String) resultMap.get(CONTEXT));
        header.setView(Long.parseLong((String)resultMap.get(VIEW)));
        header.setLike_count(Long.parseLong((String)resultMap.get(LIKES)));
        header.setUser_id(Long.parseLong((String)resultMap.get(USER_ID)));
        header.setNickname((String) resultMap.get(NICKNAME));

        return new PostDetailDto(header, postFooterDto);

    }

    private void putRedis10Minute(PostHeaderDto dto) {
        try {
            String dataKey = POST_DATA.formatKey(dto.getPost_id());
            template.opsForHash().put(dataKey, HEAD, dto.getHead());
            template.opsForHash().put(dataKey, CONTEXT, dto.getContext());
            template.opsForHash().put(dataKey, NICKNAME, dto.getNickname());
            template.opsForHash().put(dataKey, USER_ID, String.valueOf(dto.getUser_id()));
            template.expire(dataKey, 10, TimeUnit.MINUTES);

            String statsKey = POST_STATS.formatKey(dto.getPost_id());
            template.opsForHash().put(statsKey, VIEW, String.valueOf(dto.getView()));
            template.opsForHash().put(statsKey, LIKES,  String.valueOf(dto.getLike_count()));
            template.expire(statsKey, 10, TimeUnit.MINUTES);
        }catch (Exception e){
            System.out.println(e.getMessage());
            log.error("레디스 저장 중 에러 발생 : {}",dto.getPost_id());
        }
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


    public Page<MainTitleDto> searchPostByHead(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByPostHead(text, created);
    }

    public Page<MainTitleDto> searchPostByHeadOrContent(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByPostHeadOrPostContent(text, created);
    }


    public Page<MainTitleDto> searchPostByNickname(String text, PageRequest created) {
        return postRepository.findMainTitleDtoByUserNickname(text, created);
    }
}
