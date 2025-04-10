package test.crudboard.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;

@Repository
public interface RedisRepository extends CrudRepository<PostHeaderDto, Long> {

}
