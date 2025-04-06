package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.crudboard.domain.entity.like.Like;

import java.util.Optional;


@Repository
public interface LikeRepository extends JpaRepository<Like,Long> {

    boolean existsLikeByPostIdAndUserNickname(Long postId, String name);

    @Query("select m from Like m where m.post.id = :postId and m.user.nickname = :name")
    Optional<Like> findLikeByPostIdAndUsername(@Param("postId") Long postId, @Param("name") String name);


    @Modifying
    @Query("DELETE FROM Like l WHERE l.post.id = :postId AND l.user.nickname = :nickname")
    void deleteLikeByPostIdAndUserNickname(@Param("postId") Long postId, @Param("nickname") String nickname);
}
