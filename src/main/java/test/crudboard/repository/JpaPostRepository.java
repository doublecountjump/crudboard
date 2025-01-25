package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.entity.Post;
import test.crudboard.entity.dto.TitleDto;

import java.util.List;
import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {

    @Query("select new test.crudboard.entity.dto.TitleDto(p.id, p.head, p.user.email) from Post p")
    List<TitleDto> findPostList();

    @Query("select p from Post p left join fetch p.commentList c " +
            "where p.id = :id")
    Optional<Post> findPostByUserId(@Param("id") Long id);


    boolean existsPostByIdAndUserGithubId(Long postId, String githubId);
    boolean existsPostByIdAndUserEmail(Long postId, String userEmail);
}
