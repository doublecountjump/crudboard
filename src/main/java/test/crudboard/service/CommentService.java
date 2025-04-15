package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.user.User;
import test.crudboard.domain.entity.comment.dto.CommentPageDto;
import test.crudboard.domain.entity.comment.dto.PostFooterDto;
import test.crudboard.domain.type.RedisField;
import test.crudboard.repository.JpaCommentRepository;
import test.crudboard.repository.JpaPostRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static test.crudboard.domain.type.RedisField.COMMENT_COUNT;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
    private final JpaCommentRepository commentRepository;
    private final RedisService redisService;

    public Comment saveParentComment(Long postId, String context, Long userId){

        Comment comment = new Comment();
        comment.setContent(context);
        comment.setPost(Post.Quick(postId));
        comment.setUser(User.Quick(userId));
        comment.setIsParent(true);

        Comment save = commentRepository.save(comment);
        redisService.increment(postId, "comment" , 1L);

        return save;
    }

    public Comment saveChildComment(Long postId, Long parentId, String content, Long userId) {
        Comment child = new Comment();

        child.setPost(Post.Quick(postId));
        child.setContent(content);
        child.setUser(User.Quick(userId));
        child.setParent(Comment.Quick(parentId));
        child.setIsParent(false);


        Comment save = commentRepository.save(child);
        redisService.increment(postId, COMMENT_COUNT, 1L);
        return save;
    }

    public List<Comment> getCommentList(Long postId){
        return commentRepository.findCommentsByPostId(postId);
    }


    public boolean isCommentOwner(Long commentId, String name){
        return commentRepository.existsCommentByIdAndUserNickname(commentId, name);
    }

    public void deleteComment(Long postId, Long commentId) {
        commentRepository.deleteCommentById(commentId);

        long count = commentRepository.countByPostId(postId);
        redisService.update(postId,COMMENT_COUNT,String.valueOf(count));
        log.info("delete comment : {}" ,commentId);
    }

    /**
     * 댓글 목록을 받아서, 부모 자식 관계를 설정해 List<Comment>로 반환
     */
    private List<Comment> convertDtoToList(Page<CommentPageDto> commentList) {
        Map<Long, Comment> map = new HashMap<>(commentList.getSize());
        List<Comment> list = new ArrayList<>(commentList.getSize());

        for (CommentPageDto dto : commentList) {
            if(dto.isParent()){
                Comment comment = Comment.builder()
                        .id(dto.getCommentId())
                        .content(dto.getContent())
                        .isParent(true)
                        .depth(dto.getDepth())
                        .created(dto.getCreatedAt())
                        .user(User.builder()
                                .id(dto.getUserId())
                                .nickname(dto.getUsername())
                                .build())
                        .build();
                map.put(dto.getCommentId(),comment);
                list.add(comment);
            }else{
                Comment parent = map.get(dto.getParentId());

                Comment child = Comment.builder()
                        .id(dto.getCommentId())
                        .content(dto.getContent())
                        .isParent(false)
                        .depth(dto.getDepth())
                        .created(dto.getCreatedAt())
                        .user(User.builder()
                                .id(dto.getUserId())
                                .nickname(dto.getUsername())
                                .build())
                        .parent(parent)
                        .build();
                if(parent != null){
                    parent.addChild(child);
                } else{
                    list.add(child);
                }
            }
        }
        return list;
    }


    private void setPageInfo(int page, int totalPage, PostFooterDto dto) {
        int pageSize = 10; // 한 블록에 보여줄 페이지 수
        int currentBlock = (int) Math.ceil((double) page / pageSize);
        int startPage = (currentBlock - 1) * pageSize + 1;
        int endPage = Math.min(startPage + pageSize - 1, totalPage);

        dto.setStartPage(startPage);
        dto.setEndPage(endPage);
    }
}
