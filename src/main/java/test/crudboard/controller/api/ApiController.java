package test.crudboard.controller.api;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {
    private final ApiService apiService;

    @GetMapping("/popular")
    public ResponseEntity<List<PostHeaderDto>> popular(){
        List<PostHeaderDto> popularPost = apiService.getPopularPost(LocalDateTime.now());

        return ResponseEntity.ok(popularPost);
    }



}
