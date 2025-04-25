package test.crudboard.domain.entity.post.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//게시글 생성을 위한 dto
@Data
public class CreatePostDto {
    @NotBlank
    private String name;
    @NotBlank
    private String head;
    @NotBlank
    private String context;
    private List<MultipartFile> images;

    public CreatePostDto(String name){
        this.name = name;
    }
}
