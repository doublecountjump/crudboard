package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.entity.Comment;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    boolean existsCommentByIdAndUserGithubId(Long postId, String githubId);
    boolean existsCommentByIdAndUserEmail(Long postId, String userEmail);
    boolean existsCommentByIdAndUserId(Long commentId, Long userId);


    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :commentId")
    void deleteCommentById(@Param("commentId") Long commentId);
}
