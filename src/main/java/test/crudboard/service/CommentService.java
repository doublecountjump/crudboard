package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Comment;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.repository.JpaCommentRepository;
import test.crudboard.repository.JpaPostRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
    private final PostService postRepository;
    private final UserService userService;
    private final JpaCommentRepository commentRepository;

    public Comment saveParentComment(Long postId, String context, String name){
        User user = userService.findUserByNickname(name);
        Post post = postRepository.findById(postId);
        Comment comment = new Comment(post, user);
        comment.setContent(context);

        return commentRepository.save(comment);
    }

    public Comment saveChildComment(Long postId, Long parentId, String content, String name) {
        User user = userService.findUserByNickname(name);
        Post post = postRepository.findById(postId);
        Comment parent = commentRepository.findById(parentId).orElseThrow(() -> new EntityNotFoundException());
        Comment child = new Comment(post, user, parent);
        child.setContent(content);

        return commentRepository.save(child);
    }

    public boolean isCommentOwner(Long commentId, String name){
        return commentRepository.existsCommentByIdAndUserNickname(commentId, name);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteCommentById(commentId);
        log.info("delete comment : {}" ,commentId);
    }
}
