package test.crudboard.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.user.User;
import test.crudboard.repository.JpaCommentRepository;

import java.util.List;

import static test.crudboard.domain.type.RedisField.COMMENT_COUNT;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
    private final JpaCommentRepository commentRepository;
    private final RedisService redisService;

    public Comment saveParentComment(Long postId, String context, Long userId){

        Comment comment = getComment(postId,context, userId, null);
        Comment save = commentRepository.save(comment);

        redisService.increment(postId, COMMENT_COUNT , 1L);

        return save;
    }

    public Comment saveChildComment(Long postId, Long parentId, String content, Long userId) {

        Comment child = getComment(postId, content, userId, parentId);
        Comment save = commentRepository.save(child);

        redisService.increment(postId, COMMENT_COUNT, 1L);

        return save;
    }

    /**
     * comment 를 생성하는 메서드
     * @param parentId null 입력 시, 부모 댓글 생성
     */
    private static Comment getComment(Long postId, String content, Long userId ,Long parentId) {
        Comment comment = new Comment();
        comment.setPost(Post.Quick(postId));
        comment.setContent(content);
        comment.setUser(User.Quick(userId));

        if(parentId != null) {
            comment.setParent(Comment.Quick(parentId));
            comment.setIsParent(false);
        }

        else comment.setIsParent(true);

        return comment;
    }

    public List<Comment> getCommentList(Long postId){
        return commentRepository.findCommentsByPostId(postId);
    }

    public void deleteComment(Long postId, Long commentId) {
        /**
         * 부모 댓글 삭제의 경우, 자식 댓글들 까지 모두 삭제되기 때문에
         * 댓글 삭제 후 게시글의 총 댓글수를 구한 후 캐시에 저장한다
         */
        commentRepository.deleteCommentById(commentId);
        long count = commentRepository.countByPostId(postId);

        redisService.update(postId,COMMENT_COUNT,String.valueOf(count));
    }

    //댓글 주인 검증
    public boolean isCommentOwner(Long commentId, String name){
        return commentRepository.existsCommentByIdAndUserNickname(commentId, name);
    }

}
