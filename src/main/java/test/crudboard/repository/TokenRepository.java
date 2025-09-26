package test.crudboard.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.crudboard.domain.entity.token.Token;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {

    Optional<Token> findTokenByRefresh(String token);
    Optional<Token> findTokenByEmail(String email);
}
