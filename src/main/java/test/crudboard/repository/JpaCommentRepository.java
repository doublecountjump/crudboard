package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.crudboard.entity.Comment;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    boolean existsCommentByIdAndUserGithubId(Long postId, String githubId);
    boolean existsCommentByIdAndUserEmail(Long postId, String userEmail);
}
