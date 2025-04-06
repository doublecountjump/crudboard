package test.crudboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.MainTitleDto;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;

import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<Post, Long> {

    String MAIN_TITLE_SQL = "select new test.crudboard.domain.entity.post.dto.MainTitleDto(p.id, p.head, p.user.nickname, p.view, p.created," +
            "(select count(l.id) from Like l where l.post.id = p.id), "+
            "(select count(c.id) from Comment c where c.post.id =p.id)) ";

    @Query(MAIN_TITLE_SQL + "from Post p")
    Page<MainTitleDto> findPostList(Pageable page);

    //jpa  에서 join문을 만들 때, 명시적으로 join 한 것만 join함. post의 user가 eager로 설정되어도, 지정하지 않으면  지연로딩 처리됨
    @Query("select new test.crudboard.domain.entity.post.dto.PostHeaderDto" +
            "(p.id, p.head, p.context, p.view, p.like_count," +
            "u.id,u.nickname) " +
            "from Post p left join p.user u where p.id = :id")
    Optional<PostHeaderDto> findPostDetailDto(@Param("id") Long id);

    @Query(MAIN_TITLE_SQL + "from Post p where p.head like %:head%")
    Page<MainTitleDto> findMainTitleDtoByPostHead(@Param("head") String head, Pageable pageable);

    @Query(MAIN_TITLE_SQL + "from Post p where p.head like %:text% or p.context like %:text%")
    Page<MainTitleDto> findMainTitleDtoByPostHeadOrPostContent(@Param("text") String text, Pageable pageable);

    @Query(MAIN_TITLE_SQL + "from Post p where p.user.nickname like %:name%")
    Page<MainTitleDto> findMainTitleDtoByUserNickname(@Param("name") String name, Pageable pageable);

    boolean existsPostByIdAndUserNickname(Long postId, String name);




/*

   @Query("select new test.crudboard.entity.testDto(" +
            "p.id, p.head, p.view, u.nickname, " +
            "(select count(l.id) from Like l where l.post.id = p.id), " +
            "(select count(c.id) from Comment c where c.post.id =p.id))" +
            "from Post p left join p.user u")
    Page<testDto> testMethod2(Pageable pageable);
*/

}
