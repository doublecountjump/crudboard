package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import test.crudboard.entity.Like;

import java.util.Optional;


@Repository
public interface LikeRepository extends JpaRepository<Like,Long> {

    boolean existsLikeByPostIdAndUserNickname(Long postId, String name);

    @Query("select m from Like m where m.post.id = :postId and m.user.nickname = :name")
    Optional<Like> findLikeByPostIdAndUsername(@Param("postId") Long postId, @Param("name") String name);

    void deleteLikeByPostIdAndUserNickname(Long postId, String name);
}
