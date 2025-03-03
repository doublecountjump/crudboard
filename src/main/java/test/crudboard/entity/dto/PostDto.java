package test.crudboard.entity.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostDto {
    @NotBlank
    private String name;
    @NotBlank
    private String head;
    private String context;
}
