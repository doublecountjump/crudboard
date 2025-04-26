package test.crudboard.domain.post.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.domain.post.entity.Post;
import test.crudboard.domain.post.dto.PostHeaderDto;

import java.util.List;
import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {

    String MAIN_TITLE_SQL = "select new test.crudboard.domain.post.dto.PostHeaderDto(" +
            "p.id as post_id, " +
            "p.head, " +
            "p.context, " +
            "p.view, " +
            "p.created, " +
            "(select count(l) from Like l where l.post.id = p.id) as like_count, " +
            "(select count(c) from Comment c where c.post.id = p.id) as comment_count, " +
            "p.user.nickname) ";

    @Query("select p.id, p.head, p.context, p.view, p.created, "+
            "(select count(l.id) from Like l where l.post.id = p.id),  " +
            "(select count(c.id) from Comment c where c.post.id = p.id), " +
            "p.user.nickname "+
            "from Post p left join p.user u " +
            "where p.id between :start and :end " +
            "order by p.id desc limit 20")
    List<Object[]> findPostList(@Param("start")long start, @Param("end") long end);

    @Query(MAIN_TITLE_SQL +
            "from Post p left join p.user u where p.id = :id")
    Optional<PostHeaderDto> findPostHeaderDto(@Param("id") Long id);

    @Query(MAIN_TITLE_SQL + "from Post p where p.head like %:head%")
    Page<PostHeaderDto> findMainTitleDtoByPostHead(@Param("head") String head, Pageable pageable);

    @Query(MAIN_TITLE_SQL + "from Post p where p.head like %:text% or p.context like %:text%")
    Page<PostHeaderDto> findMainTitleDtoByPostHeadOrPostContent(@Param("text") String text, Pageable pageable);

    @Query(MAIN_TITLE_SQL + "from Post p where p.user.nickname like %:name%")
    Page<PostHeaderDto> findMainTitleDtoByUserNickname(@Param("name") String name, Pageable pageable);

    boolean existsPostByIdAndUserNickname(Long postId, String name);

    @Query("select count(p.id) from Post p")
    long count();

    @Query("select max(p.id) from Post p")
    Long maxPostId();
}