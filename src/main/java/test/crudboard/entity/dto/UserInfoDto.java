package test.crudboard.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.crudboard.entity.Comment;
import test.crudboard.entity.Post;
import test.crudboard.entity.enumtype.AuthProvider;
import test.crudboard.entity.enumtype.Roles;

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
