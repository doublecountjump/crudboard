package test.crudboard.domain.entity.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.enumtype.AuthProvider;
import test.crudboard.domain.entity.enumtype.Roles;
import test.crudboard.domain.entity.post.Post;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private Long id;
    private String username;
    private String email;
    private String avatar_url;
    private String githubId;
    private AuthProvider provider;
    private Roles roles;
    private List<Post> postList;
    private List<Comment> commentList;
}
