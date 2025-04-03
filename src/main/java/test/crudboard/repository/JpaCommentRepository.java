package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    boolean existsCommentByIdAndUserNickname(Long postId, String name);

    @Query("select c from Comment c " +
            "left join fetch c.child h " +
            "left join fetch c.user  " +
            "left join fetch h.user  " +
            "where c.post.id = :id and c.parent is null " +
            "order by c.id desc")
    List<Comment> findCommentsByPostId(@Param("id") Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :commentId")
    void deleteCommentById(@Param("commentId") Long commentId);
}
