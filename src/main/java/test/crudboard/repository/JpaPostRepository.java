package test.crudboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.entity.Post;
import test.crudboard.entity.dto.TitleDto;
import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {

    @Query("select new test.crudboard.entity.dto.TitleDto(p.id, p.head, p.user.email, p.view, p.created) from Post p")
    Page<TitleDto> findPostList(Pageable page);

    @Query("select p from Post p left join fetch p.commentList c " +
            "where p.id = :id")
    Optional<Post> findPostByUserId(@Param("id") Long id);

    @Query("select count(*) from Post p")
    Long getCount();

    boolean existsPostByIdAndUserNickname(Long postId, String name);
}
