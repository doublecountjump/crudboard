package test.crudboard.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String head;
    private String context;
    private Long view;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;


    @OneToMany(mappedBy = "post",fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Like> likeList = new ArrayList<>();

    public int getLikeCount() {
        return likeList.size();
    }

    public boolean isLikedByUser(String name) {
        if(name == null) return false;
        return likeList.stream()
                .anyMatch(like -> like.getUser().getNickname().equals(name));
    }

    public void setUser(User user){
        this.user = user;
        user.getPostList().add(this);
    }
}
