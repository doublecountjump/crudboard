package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.crudboard.domain.entity.user.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface JpaUserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByNickname(@Param("name") String name);
    boolean existsUserByIdAndNickname(Long id, String name);

    @Query("select u.commentList, u.postList from User u where u.nickname = :name")
    List<Object> find(@Param("name") String name);

}
