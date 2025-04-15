package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.like.Like;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.error.AlreadyLikedException;
import test.crudboard.domain.error.ErrorCode;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final JpaUserRepository userRepository;
    private final JpaPostRepository postRepository;
    /**
     * 현재 사용자를 게시글의 추천자로 등록
     * @param postId
     * @param id
     */
    @Transactional
    public void recommendPost(Long postId, Long userId) {
        boolean exist = likeRepository.existsLikeByPostIdAndUserId(postId, userId);

        if(exist){
            throw new AlreadyLikedException(ErrorCode.USER_HAS_ALREADY_LIKED_POST);
        }else{
            Like like = Like.builder()
                    .user(User.Quick(userId))
                    .post(Post.Quick(postId))
                    .build();

            likeRepository.save(like);
        }
    }
}
