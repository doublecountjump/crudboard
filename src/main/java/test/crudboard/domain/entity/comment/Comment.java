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

    //양방향 연관관계 설정
    public void setPost(Post post) {
        this.post = post;
        post.getCommentList().add(this);
    }

    //양방향 연관관계 설정
    public void setUser(User user) {
        this.user = user;
        user.getCommentList().add(this);
    }

    //이름이 setparent와 중복되어 에러가 발생했기에, 따로 수정
    public void setIsParent(boolean isParent){
        this.isParent = isParent;
    }

    //양방향 연관관계 설정

    public void addChild(Comment comment) {
        if (!comment.isParent) {
            this.child.add(comment);
            comment.setParent(this);
        }else System.out.println("!comment.isParent error");
    }


    /**
     *  jpa를 통한 엔티티를 저장하기 위해, 연관관계 설정을 위해 필요한 id 필드만 저장해 새 객체 생성
     */
    private Comment(Long id){
        this.id = id;
    }


    public static Comment Quick(Long id){
        return new Comment(id);
    }
}
