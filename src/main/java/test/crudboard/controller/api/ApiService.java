package test.crudboard.controller.api;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.repository.JpaPostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiService {
    private final JpaPostRepository repository;

    public List<PostHeaderDto> getPopularPost(LocalDateTime now) {
        LocalDateTime yesterday = now.minusDays(1);
        return repository.findPopularPostList(yesterday, now);
    }
}
