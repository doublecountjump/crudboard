package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.crudboard.entity.User;

import java.util.Optional;


@Repository
public interface JpaUserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByNickname(String name);
    boolean existsUserByIdAndNickname(Long id, String name);
}
