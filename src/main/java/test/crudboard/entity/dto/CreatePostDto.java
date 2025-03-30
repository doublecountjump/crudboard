package test.crudboard.entity.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
