package test.crudboard.domain.entity.comment;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private String content;


    @CreatedDate
    @Column(updatable = false)
    LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    private List<Comment> child = new ArrayList<>();

    private Long depth;
    boolean isParent;

    public boolean isParent(){
        return this.isParent;
    }

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

    public void addChild(Comment comment) {
        if (!comment.isParent) {
            this.child.add(comment);
            comment.setParent(this);
        }else System.out.println("!comment.isParent error");
    }
}
