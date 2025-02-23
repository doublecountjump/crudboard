package test.crudboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.crudboard.entity.enumtype.AuthProvider;
import test.crudboard.entity.enumtype.Roles;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "users")
@Builder
@NoArgsConstructor  // 이 어노테이션 추가
@AllArgsConstructor // Builder를 위해 필요
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;


    private String nickname;
    private String email;        // 구별
    private String password;     // 일반 로 그인용 (OAuth2 사용자는 null)

    private String githubId;     // OAuth2 로그인용 (일반 사용자는 null)
    private String profileImage; // OAuth2 로그인용 (일반 사용자는 null)

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // LOCAL, GITHUB, GOOGLE 등

    @Enumerated(EnumType.STRING)
    private Roles roles;


    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> postList = new ArrayList<>();

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();
}