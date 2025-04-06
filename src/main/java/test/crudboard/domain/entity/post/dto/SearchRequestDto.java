package test.crudboard.domain.entity.post.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import test.crudboard.domain.entity.enumtype.SearchType;

@Data
public class SearchRequestDto {
    private SearchType type;
    @NotBlank
    private String content;
    private int page;
}
