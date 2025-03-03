package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
@Transactional
public class CommentService {
    private final JpaPostRepository postRepository;
    private final JpaUserRepository userRepository;
    private final JpaCommentRepository commentRepository;
    public Comment saveParentComment(Long postId, String context, String email){
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        Comment comment = new Comment(post, user);
        comment.setContent(context);

        return commentRepository.save(comment);
    }

    public Comment saveChildComment(Long postId, Long parentId, String content, String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
        Comment parent = commentRepository.findById(parentId).orElseThrow(() -> new EntityNotFoundException());
        Comment child = new Comment(post, user, parent);
        child.setContent(content);

        return commentRepository.save(child);
    }

    public boolean isCommentOwner(Long commentId, String name){
        return commentRepository.existsCommentByIdAndUserNickname(commentId, name);
    }

    public void deleteComment(Long commentId) {
        System.out.println("delete : " + commentId);
        commentRepository.deleteCommentById(commentId);
        log.info("delete comment : {}" ,commentId);
    }
}
