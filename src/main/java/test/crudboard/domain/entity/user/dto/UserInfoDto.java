package test.crudboard.domain.entity.user.dto;


import lombok.*;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.user.User;
import test.crudboard.security.type.AuthProvider;
import test.crudboard.security.type.Roles;
import test.crudboard.domain.entity.post.Post;

import java.util.List;


//사용자의 정보를 저장
@Getter
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

    public UserInfoDto(User user) {
        this.id = user.getId();
        this.username = user.getNickname();
        this.email = user.getEmail();
        this.avatar_url = user.getProfileImage();
        this.githubId = user.getGithubId();
        this.provider = user.getProvider();
        this.roles = user.getRoles();
    }
}
