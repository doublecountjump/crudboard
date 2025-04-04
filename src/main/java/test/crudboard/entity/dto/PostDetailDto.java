package test.crudboard.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDetailDto {
    private PostHeaderDto header;
    private PostFooterDto footer;
}
