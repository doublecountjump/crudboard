package test.crudboard.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import test.crudboard.entity.Comment;
import test.crudboard.entity.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Repository
public interface JpaUserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByNickname(@Param("name") String name);
    boolean existsUserByIdAndNickname(Long id, String name);


}
