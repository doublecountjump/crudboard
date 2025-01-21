package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.PostDto;
import test.crudboard.entity.dto.TitleDto;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService{
    private final JpaUserRepository userRepository;
    private final JpaPostRepository postRepository;

    public Post save(PostDto postDto){
        User user = userRepository.findById(postDto.getUserid())
                .orElseThrow(() -> new EntityNotFoundException("entity not found"));

        Post post = Post.builder()
                .head(postDto.getHead())
                .context(postDto.getHead())
                .build();
        post.setUser(user);

        return postRepository.save(post);
    }
    public List<TitleDto> getTitleList(){
        return postRepository.findPostList();
    }

    public Post getPostById(Long id){
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("entity not found"));
    }

    public boolean isGithubPostOwner(Long postId, String githubId){
        return postRepository.existsPostByIdAndUserGithubId(postId, githubId);
    }

    public boolean isLocalPostOwner(Long postId, String userEmail){
        return postRepository.existsPostByIdAndUserEmail(postId, userEmail);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
        log.info("Delete Post! id : {}", id);
    }
}
