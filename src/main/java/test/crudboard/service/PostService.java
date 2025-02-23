package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Like;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.PostDto;
import test.crudboard.entity.dto.TitleDto;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.LikeRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PostService{
    private final JpaUserRepository userRepository;
    private final JpaPostRepository postRepository;
    private final LikeRepository likeRepository;

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
        return postRepository.findPostList();
    }

    public Post getPostById(Long id){
        return postRepository.findPostByUserId(id).orElseThrow(() -> new EntityNotFoundException("entity not found"));
    }



    public boolean isGithubPostOwner(Long postId, String githubId){
        return postRepository.existsPostByIdAndUserGithubId(postId, githubId);
    }

    public boolean isPostOwner(Long postId, String userEmail){
        return postRepository.existsPostByIdAndUserEmail(postId, userEmail);
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
