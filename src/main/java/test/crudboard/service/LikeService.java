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
import test.crudboard.domain.type.RedisField;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final RedisService redisService;

    /**
     * 게시글의 추천 목록을 추가하는 메서드
     * @param postId 추천한 게시글
     * */
    @Transactional
    public void recommendPost(Long postId, Long userId) {

        //이미 추천을 했는지 확인
        boolean exist = likeRepository.existsLikeByPostIdAndUserId(postId, userId);

        if(exist){
            //추천했다면, 변화를 주지 않음
            throw new AlreadyLikedException(ErrorCode.USER_HAS_ALREADY_LIKED_POST);
        }
        else{
            //추천을 안한 사용자일 경우, 새 like 생성
            Like like = Like.builder()
                    .user(User.Quick(userId))
                    .post(Post.Quick(postId))
                    .build();
            likeRepository.save(like);
            redisService.increment(postId, RedisField.LIKE_COUNT, 1L);
        }
    }
}
