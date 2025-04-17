package test.crudboard.domain.entity.post;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.like.Like;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String head;
    private String context;
    private Long view;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime created;
    @LastModifiedDate
    LocalDateTime modify;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post",fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likeList = new ArrayList<>();

    public void setUser(User user){
        this.user = user;
        user.getPostList().add(this);
    }

    private Post(Long id){
        this.id = id;
    }

    public static Post Quick(Long id){
        return new Post(id);
    }
}
