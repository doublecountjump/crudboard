package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import test.crudboard.domain.aop.valid.CheckResourceOwner;
import test.crudboard.domain.aop.valid.ResourceType;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.CommentService;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comment")
@PreAuthorize("isAuthenticated()")
public class CommentController {
    private final CommentService commentService;


    @PostMapping("/{postId}")
    public String addComment(@PathVariable Long postId, String content,
                             @AuthenticationPrincipal JwtUserDetails user){
        //부모 댓글 저장
        commentService.saveParentComment(postId, content, user.getId());

        return "redirect:/post/detail/" + postId;
    }

    @PostMapping("/{postId}/{parentId}")
    public String addChildComment(@PathVariable Long postId, @PathVariable Long parentId,
                                  String content, @AuthenticationPrincipal JwtUserDetails user){

        //자식 댓글 저장
        commentService.saveChildComment(postId, parentId, content, user.getId());

        return "redirect:/post/detail/" + postId;
    }


    @DeleteMapping("/{postId}/{commentId}")
    @CheckResourceOwner(type = ResourceType.COMMENT)
    @ResponseBody
    public void deletePost(@PathVariable Long commentId, @AuthenticationPrincipal Object user,
                           @PathVariable Long postId){
        //해당 댓글 삭제, 부모댓글의 경우 자식댓글들 또한 삭제
        commentService.deleteComment(postId, commentId);

    }

}
