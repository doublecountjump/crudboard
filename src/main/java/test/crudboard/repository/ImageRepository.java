package test.crudboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.crudboard.domain.entity.image.Image;

import java.util.List;


@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select i.url from Image i where i.post.id = :id")
    List<String> findByPostId(@Param("id") Long id);
}
