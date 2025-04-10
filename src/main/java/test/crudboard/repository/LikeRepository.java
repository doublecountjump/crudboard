package test.crudboard.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.crudboard.domain.entity.like.Like;

import java.util.Optional;


@Repository
public interface LikeRepository extends JpaRepository<Like,Long> {

    @Query("select exists (select 1 from Like l where l.post_id = :postId and l.nickname = :name)")
    boolean existsLikeByPost_idAndNickname(@Param("postId") Long postId, @Param("name") String name);

    @Query("select m from Like m where m.post_id = :postId and m.nickname = :name")
    Optional<Like> findLikeByPostIdAndUsername(@Param("postId") Long postId, @Param("name") String name);


    @Modifying
    @Query("DELETE FROM Like l WHERE l.post_id = :postId AND l.nickname = :nickname")
    void deleteLikeByPostIdAndUserNickname(@Param("postId") Long postId, @Param("nickname") String nickname);
}
