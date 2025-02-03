package test.crudboard.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.crudboard.controller.TokenController;
import test.crudboard.entity.Token;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {

    Optional<Token> findTokenByRefresh(String token);
    Optional<Token> findTokenByEmail(String email);
}
