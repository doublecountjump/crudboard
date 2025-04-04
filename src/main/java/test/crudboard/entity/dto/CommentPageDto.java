package test.crudboard.entity.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Comment;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageDto {
    private Long commentId;
    private String content;
    private Long parentId;
    private boolean isParent;
    private Long depth;
    private LocalDateTime createdAt;

    private Long postId;

    private Long userId;
    private String username;

}
