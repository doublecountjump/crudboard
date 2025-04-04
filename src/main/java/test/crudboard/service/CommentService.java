package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import test.crudboard.entity.Comment;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.CommentPageDto;
import test.crudboard.entity.dto.PostFooterDto;
import test.crudboard.repository.JpaCommentRepository;
import test.crudboard.repository.JpaPostRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
    private final JpaPostRepository postRepository;
    private final UserService userService;
    private final JpaCommentRepository commentRepository;

    public Comment saveParentComment(Long postId, String context, String name){
        User user = userService.findUserByNickname(name);
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("comment entity not found"));
        Comment comment = new Comment(post, user);
        comment.setContent(context);

        return commentRepository.save(comment);
    }

    public Comment saveChildComment(Long postId, Long parentId, String content, String name) {
        User user = userService.findUserByNickname(name);
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("comment entity not found"));
        Comment parent = commentRepository.findById(parentId).orElseThrow(() -> new EntityNotFoundException());
        Comment child = new Comment(post, user, parent);
        child.setContent(content);

        return commentRepository.save(child);
    }

    public PostFooterDto getCommentListDto(Long postId, Integer page){
        PageRequest pageRequest = PageRequest.of(page - 1, 20, Sort.by("created").ascending());
        Page<CommentPageDto> commentList = commentRepository.findCommentPageByPostId(postId, pageRequest);

        PostFooterDto responseDto = new PostFooterDto();
        responseDto.setCommentList(convertDtoToList(commentList));
        setPageInfo(page, commentList.getTotalPages(), responseDto);

        return responseDto;
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
        int currentBlock = (int) Math.ceil(page / pageSize);
        int startPage = (currentBlock - 1) * pageSize + 1;
        int endPage = Math.min(startPage + pageSize - 1, totalPage);

        dto.setStartPage(startPage);
        dto.setEndPage(endPage);
    }

    public boolean isCommentOwner(Long commentId, String name){
        return commentRepository.existsCommentByIdAndUserNickname(commentId, name);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteCommentById(commentId);
        log.info("delete comment : {}" ,commentId);
    }
}
