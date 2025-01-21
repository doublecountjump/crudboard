package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import test.crudboard.annotation.CheckResourceOwner;
import test.crudboard.entity.Post;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.PostDto;
import test.crudboard.entity.enumtype.ResourceType;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.provider.local.LocalUserDetailsService;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(@AuthenticationPrincipal Object user, Model model){
        String userEmail = getUserEmail(user);
        User authUser = userService.findUserByEmail(userEmail);
        model.addAttribute("userId",authUser.getId());
        return "post";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@ModelAttribute PostDto postDto){
        Post save = postService.save(postDto);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String detailPost(@PathVariable Long id, Model model){
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        return "post-detail.html";
    }


    @DeleteMapping("/{id}")
    @CheckResourceOwner(type = ResourceType.POST)
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal Object user){
        postService.deletePost(id);
        return "redirect:/";
    }
    private String getUserEmail(Object user) {
        if (user instanceof OAuth2User auth2User) {
            return auth2User.getName();
        } else if (user instanceof LocalUserDetails localUser) {
            return localUser.getUsername();
        }
        throw new IllegalArgumentException("Unsupported user type");
    }
}
