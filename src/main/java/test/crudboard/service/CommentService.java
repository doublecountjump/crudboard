package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Comment;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.repository.JpaCommentRepository;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.repository.JpaUserRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final JpaPostRepository postRepository;
    private final JpaUserRepository userRepository;
    private final JpaCommentRepository commentRepository;
    public Comment saveParentComment(Long postId, String context, Map<String,String> userType){
        User user = getUser(userType);
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        Comment comment = new Comment(post, user);
        comment.setContent(context);

        return commentRepository.save(comment);
    }

    public Comment saveChildComment(Long postId, Long parentId, String content, Map<String, String> userType) {
        User user = getUser(userType);
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        Comment parent = commentRepository.findById(parentId).orElseThrow(() -> new EntityNotFoundException());
        Comment child = new Comment(post, user, parent);
        child.setContent(content);

        return commentRepository.save(child);
    }

    private User getUser(Map<String, String> userType){
        User user;
        if(userType.containsKey("oauth")){
            user = userRepository.findUserByGithubId(userType.get("oauth"))
                    .orElseThrow(() -> new EntityNotFoundException("entity not found"));
        } else if (userType.containsKey("local")) {
            user = userRepository.findUserByEmail(userType.get("local"))
                    .orElseThrow(() -> new EntityNotFoundException("entity not found"));
        }else throw new BadCredentialsException("bad");

        return user;
    }

    public boolean isGithubCommentOwner(Long commentId, String name) {
        return commentRepository.existsCommentByIdAndUserGithubId(commentId, name);
    }

    public boolean isLocalCommentOwner(Long commentId, String name){
        return commentRepository.existsCommentByIdAndUserEmail(commentId, name);
    }
}
