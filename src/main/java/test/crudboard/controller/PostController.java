package test.crudboard.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import test.crudboard.domain.aop.annotation.CheckResourceOwner;
import test.crudboard.domain.entity.post.dto.CreatePostDto;
import test.crudboard.domain.entity.post.dto.PostDetailDto;
import test.crudboard.domain.entity.enumtype.ResourceType;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.LikeService;
import test.crudboard.service.PostService;


/**
 * 0203 getUserEmail 수정할것!!
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final LikeService likeService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(@AuthenticationPrincipal JwtUserDetails user, Model model){
        model.addAttribute("createPostDto",new CreatePostDto(user.getUsername()));
        return "post";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute CreatePostDto createPostDto, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "post";
        }
        postService.save(createPostDto);
        return "redirect:/";
    }

    @GetMapping(value = {"/{postId}/{page}","/{postId}"})
    public String getDetailPost(@PathVariable Long postId, @PathVariable(required = false) Integer page,@AuthenticationPrincipal JwtUserDetails user, Model model){
        if(page == null){
            page = 1;
        }
        PostDetailDto dto = postService.getPostDetailDtoById(postId, page);
        model.addAttribute("header", dto.getHeader());
        model.addAttribute("footer", dto.getFooter());
        model.addAttribute("currentUserNickname", user != null ? user.getUsername() : null);
        model.addAttribute("page", page);

        return "post-detail";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recommend/{postId}")
    public String recommendPost(@PathVariable Long postId, @AuthenticationPrincipal JwtUserDetails user){
        likeService.recommendPost(postId,user.getUsername());

        return "redirect:/post/" + postId;
    }


    @DeleteMapping("/{id}")
    @CheckResourceOwner(type = ResourceType.POST)
    @ResponseBody
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails user){
        postService.deletePost(id);
    }
}
