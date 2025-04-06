package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import test.crudboard.domain.aop.annotation.CheckResourceOwner;
import test.crudboard.domain.entity.enumtype.ResourceType;
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

        commentService.saveParentComment(postId, content, user.getUsername());

        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/{parentId}")
    public String addReplyComment(@PathVariable Long postId, @PathVariable Long parentId,
                                  String content, @AuthenticationPrincipal JwtUserDetails user){
        commentService.saveChildComment(postId, parentId, content, user.getUsername());

        return "redirect:/post/" + postId;
    }


    @DeleteMapping("/{postId}/{commentId}")
    @CheckResourceOwner(type = ResourceType.COMMENT)
    @ResponseBody
    public void deletePost(@PathVariable Long commentId, @AuthenticationPrincipal Object user,
                           @PathVariable Long postId){
        commentService.deleteComment(commentId);
    }

}
