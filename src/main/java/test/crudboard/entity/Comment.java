package test.crudboard.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> child = new ArrayList<>();

    private Long depth;
    boolean isParent;

    public Comment(Post post, User user, Comment parent){
        this.setPost(post);
        this.setUser(user);
        this.parent = parent;
        parent.getChild().add(this);
        this.depth = parent.getDepth()+1L;
        this.isParent =false;
    }

    public Comment(Post post, User user){
        this.setPost(post);
        this.setUser(user);
        this.depth = 0L;
        this.isParent = true;
    }

    public void setPost(Post post) {
        this.post = post;
        post.getCommentList().add(this);
    }

    public void setUser(User user) {
        this.user = user;
        user.getCommentList().add(this);
    }


}
