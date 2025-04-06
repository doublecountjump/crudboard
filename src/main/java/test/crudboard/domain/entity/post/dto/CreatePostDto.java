package test.crudboard.domain.entity.post.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePostDto {
    @NotBlank
    private String name;
    @NotBlank
    private String head;
    @NotBlank
    private String context;

    public CreatePostDto(String name){
        this.name = name;
    }
}
