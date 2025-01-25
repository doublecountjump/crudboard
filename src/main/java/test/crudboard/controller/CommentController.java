package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import test.crudboard.entity.Comment;
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
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;


    @PostMapping("/{postId}")
    public String addComment(@PathVariable Long postId, String content,
                             @AuthenticationPrincipal Object user){

        log.info("context :{} ", content);
        String userEmail = getUserEmail(user);

        Comment comment = commentService.saveParentComment(postId, content, userEmail);

        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/{parentId}")
    public String addReplyComment(@PathVariable Long postId, @PathVariable Long parentId,
                                  String content, @AuthenticationPrincipal Object user){
        String userEmail = getUserEmail(user);
        commentService.saveChildComment(postId, parentId, content, userEmail);

        return "redirect:/post/" + postId;
    }

    private String getUserEmail(Object user){
        if(user instanceof OAuth2User auth2User){
            return auth2User.getName();
        }else if(user instanceof LocalUserDetails localUser){
            return localUser.getUsername();
        }else throw new BadCredentialsException("사용자가 올바르지 않습니다");
    }

}
