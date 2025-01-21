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
        Map<String, String> userType = getUserType(user);

        Comment comment = commentService.saveParentComment(postId, content, userType);

        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/{parentId}")
    public String addReplyComment(@PathVariable Long postId, @PathVariable Long parentId,
                                  String content, @AuthenticationPrincipal Object user){
        Map<String, String> userType = getUserType(user);
        commentService.saveChildComment(postId, parentId, content, userType);

        return "redirect:/post/" + postId;
    }

    private Map<String, String> getUserType(Object user){
        if(user instanceof OAuth2User auth2User){
            HashMap<String, String> map = new HashMap<>();
            map.put("oauth",auth2User.getName());
            return map;
        }else if(user instanceof LocalUserDetails localUser){
            HashMap<String, String> map = new HashMap<>();
            map.put("local",localUser.getUsername());
            return map;
        }else throw new BadCredentialsException("사용자가 올바르지 않습니다");
    }

}
