package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import test.crudboard.annotation.CheckResourceOwner;
import test.crudboard.entity.Comment;
import test.crudboard.entity.enumtype.ResourceType;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.service.CommentService;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;

import java.util.HashMap;
import java.util.Map;

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
        System.out.println("delete controller");
        commentService.deleteComment(commentId);
    }

}
