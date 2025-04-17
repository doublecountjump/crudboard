package test.crudboard.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;

import java.util.List;
import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {

    String MAIN_TITLE_SQL = "select new test.crudboard.domain.entity.post.dto.PostHeaderDto(" +
            "p.id as post_id, " +
            "p.head, " +
            "p.context, " +
            "p.view, " +
            "p.created, " +
            "(select count(l) from Like l where l.post.id = p.id) as like_count, " +
            "(select count(c) from Comment c where c.post.id = p.id) as comment_count, " +
            "p.user.nickname) ";
/*    @Query(value = "SELECT p.post_id, p.head, p.context, p.view, p.created, " +
            "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.post_id) AS like_count, " +
            "(SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id) AS comment_count, " +
            "u.nickname " +
            "FROM post p " +
            "LEFT OUTER JOIN users u ON p.user_id = u.user_id " +
            "ORDER BY p.post_id DESC",
            countQuery = "SELECT COUNT(*) FROM post",
            nativeQuery = true)*/
    @Query("select p.id, p.head, p.context, p.view, p.created, "+
            "(select count(l) from Like l where l.post.id = p.id),  " +
            "(select count(c) from Comment c where c.post.id = p.id), " +
            "p.user.nickname "+
            "from Post p left join p.user u")
    Page<Object[]> findPostList(Pageable page);

    //jpa  에서 join문을 만들 때, 명시적으로 join 한 것만 join함. post의 user가 eager로 설정되어도, 지정하지 않으면  지연로딩 처리됨
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


    @Query("select p from Post p left join p.user u order by p.id desc ")
    List<Post> findAllLimit(Pageable page);

    @Query("select count(p.id) from Post p")
    long count();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdWithLock(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.view = p.view + 1 WHERE p.id = :postId")
    void IncrementViewCount(@Param("postId") Long postId);

/*

   @Query("select new test.crudboard.entity.testDto(" +
            "p.id, p.head, p.view, u.nickname, " +
            "(select count(l.id) from Like l where l.post.id = p.id), " +
            "(select count(c.id) from Comment c where c.post.id =p.id))" +
            "from Post p left join p.user u")
    Page<testDto> testMethod2(Pageable pageable);
*/

}
