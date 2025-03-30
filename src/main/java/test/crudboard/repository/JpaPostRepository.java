package test.crudboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.entity.Post;
import test.crudboard.entity.dto.MainTitleDto;
import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {

    @Query("select new test.crudboard.entity.dto.MainTitleDto(p.id, p.head, p.user.nickname, p.view, p.created) from Post p")
    Page<MainTitleDto> findPostList(Pageable page);

    @Query("select p from Post p left join fetch p.commentList c " +
            "where p.id = :id")
    Optional<Post> findPostByUserId(@Param("id") Long id);

    @Query("select count(*) from Post p")
    Long getCount();

    @Query("select new test.crudboard.entity.dto.MainTitleDto(p.id, p.head, p.user.nickname, p.view, p.created) from Post p where p.head like %:head%")
    Page<MainTitleDto> findMainTitleDtoByPostHead(@Param("head") String head, Pageable pageable);

    @Query("select new test.crudboard.entity.dto.MainTitleDto(p.id, p.head, p.user.nickname, p.view, p.created) from Post p where p.head like %:text% or p.context like %:text%")
    Page<MainTitleDto> findMainTitleDtoByPostHeadOrPostContent(@Param("text") String text, Pageable pageable);

    @Query("select new test.crudboard.entity.dto.MainTitleDto(p.id, p.head, p.user.nickname, p.view, p.created) from Post p where p.user.nickname like %:name%")
    Page<MainTitleDto> findMainTitleDtoByUserNickname(@Param("name") String name, Pageable pageable);

    boolean existsPostByIdAndUserNickname(Long postId, String name);
}
