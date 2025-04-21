package test.crudboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.domain.entity.comment.Comment;


import java.util.List;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    boolean existsCommentByIdAndUserNickname(Long postId, String name);

    @Query("select distinct c from Comment c " +
            "left join fetch c.user  " +
            "where c.post.id = :id " +
            "order by coalesce(c.parent.id, c.id) , c.isParent desc , c.id asc")
    List<Comment> findCommentsByPostId(@Param("id") Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :commentId")
    void deleteCommentById(@Param("commentId") Long commentId);


    long countByPostId(Long postId);

}
