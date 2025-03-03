package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import test.crudboard.annotation.CheckResourceOwner;
import test.crudboard.entity.dto.PostDto;
import test.crudboard.entity.dto.PostResponseDto;
import test.crudboard.entity.enumtype.ResourceType;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;


/**
 * 0203 getUserEmail 수정할것!!
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(@AuthenticationPrincipal JwtUserDetails user, Model model){
        model.addAttribute("name",user.getUsername());
        return "post";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@ModelAttribute PostDto postDto){
        postService.save(postDto);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String detailPost(@PathVariable Long id,@AuthenticationPrincipal JwtUserDetails user, Model model){
        PostResponseDto dto = postService.getPostResponseDtoById(id);
        model.addAttribute("post", dto.getPost());
        model.addAttribute("view", dto.getView());
        model.addAttribute("currentUserNickname", user != null ? user.getUsername() : null);
        return "post-detail.html";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}")
    public String recommendPost(@PathVariable Long postId, @AuthenticationPrincipal JwtUserDetails user){
        postService.recommendPost(postId,user.getUsername());
        return "redirect:/post/" + postId;
    }


    @DeleteMapping("/{id}")
    @CheckResourceOwner(type = ResourceType.POST)
    @ResponseBody
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails user){
        postService.deletePost(id);
    }
}
