package test.crudboard.domain.entity.post.dto;


import lombok.Data;
import lombok.Generated;
import lombok.Getter;

import java.util.List;

@Getter
public class ResultDto {
    private final PostDetailDto dto;
    private final List<String> img;

    public ResultDto(PostDetailDto dto, List<String> img){
        this.dto = dto;
        this.img = img;
    }
}
