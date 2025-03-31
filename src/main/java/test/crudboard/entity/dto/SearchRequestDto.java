package test.crudboard.entity.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import test.crudboard.entity.enumtype.SearchType;

@Data
public class SearchRequestDto {
    private SearchType type;
    @NotBlank
    private String content;
    private int page;
}
