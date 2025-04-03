package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Like;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.CreatePostDto;
import test.crudboard.entity.dto.DetailPostDto;
import test.crudboard.entity.dto.MainTitleDto;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.LikeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Objects;


/**
 * 게시글에서 가장 자주 사용되는 기능을 생성(게시글, 댓글) 그리고 게시글 조회로 가정함
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class  PostService{
    private final JpaUserRepository userRepository;
    private final JpaPostRepository postRepository;
    private final LikeRepository likeRepository;
    private final RedisTemplate<String, String> template;
    private static final String VIEW_COUNT = "view:content:";
    private static final String POST_DATA = "post:data:";
    private static final String POST_STATUS = "post:status:";
    //게시글 저장
    public Post save(CreatePostDto createPostDto){
        User user = userRepository.findUserByNickname(createPostDto.getName())
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        Post post = Post.builder()
                .head(createPostDto.getHead())
                .context(createPostDto.getContext())
                .view(0L)
                .build();
        post.setUser(user);

        return postRepository.save(post);
    }

    public Post findById(Long postId){
        return postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("not found!!"));
    }

    /**
     * 메인화면의 타이틀 목록을 가져옴
     * @param page 가져오고자 하는 페이지
     */
    public Page<MainTitleDto> getTitleList(Integer page){
        PageRequest created = PageRequest.of(page - 1, 5, Sort.by("created").descending());
        Page<MainTitleDto> postList = postRepository.findPostList(created);

        for (MainTitleDto mainTitleDto : postList) {
            String key = VIEW_COUNT + mainTitleDto.getId();
            String view = template.opsForValue().get(key);

            if(view!=null){
                mainTitleDto.setView(Long.parseLong(view));
            }else{
                mainTitleDto.setView(0L);
            }
        }
        
        return postList;
    }

    /**
     * 해당하는 id의 게시글을 가져옴
     * @param id 찾고자 하는 게시글
     * @return
     */
    public DetailPostDto getDetailPostDtoById(Long id){
        String key = VIEW_COUNT + id;
        Post post = postRepository.findPostByPostId(id).orElseThrow(() -> new EntityNotFoundException("entity not found"));

        //redis 에서 게시글의 조회수를 가져옴, 없으면 추가
        if(template.hasKey(key)){
            template.opsForValue().increment(key);
        }else {
            template.opsForValue().set(key,String.valueOf(post.getView() + 1));
        }
        Long view = Long.parseLong(Objects.requireNonNull(template.opsForValue().get(key)));
        System.out.println("service end");
        return new DetailPostDto(post, view);
    }

    public boolean isPostOwner(Long postId, String name){
        return postRepository.existsPostByIdAndUserNickname(postId, name);
    }

    public Post update(Long postId, CreatePostDto postDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        post.setHead(postDto.getHead());
        post.setContext(postDto.getContext());

        return post;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
        log.info("Delete Post! id : {}", id);
    }

    /**
     * 현재 사용자를 게시글의 추천자로 등록
     * @param postId
     * @param name
     */
    public void recommendPost(Long postId, String name) {
        boolean b = likeRepository.existsLikeByPostIdAndUserNickname(postId, name);
        if(b){
            likeRepository.deleteLikeByPostIdAndUserNickname(postId, name);
        }else{

            Like like = Like.builder()
                    .user(userRepository.findUserByNickname(name).orElseThrow(() -> new EntityNotFoundException("user not found")))
                    .post(postRepository.findById(postId).orElseThrow(()-> new EntityNotFoundException("post not found")))
                    .build();

            likeRepository.save(like);
        }
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
