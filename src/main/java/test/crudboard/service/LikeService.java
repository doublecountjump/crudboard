package test.crudboard.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.crudboard.domain.entity.like.Like;
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
     * @param name
     */
    @Transactional
    public void recommendPost(Long postId, String name) {
        boolean exist = likeRepository.existsLikeByPost_idAndNickname(postId, name);

        if(exist){
            likeRepository.deleteLikeByPostIdAndUserNickname(postId, name);
        }else{
            Like like = new Like();
            like.setPost_id(postId);
            like.setNickname(name);
            likeRepository.save(like);
        }
    }
}
