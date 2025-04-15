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
import test.crudboard.domain.entity.post.Post;
import test.crudboard.domain.entity.post.dto.CreatePostDto;
import test.crudboard.domain.entity.post.dto.PostDetailDto;
import test.crudboard.domain.entity.enumtype.ResourceType;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.LikeService;
import test.crudboard.service.PostService;
import test.crudboard.service.RedisService;


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
    private final RedisService redisService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(@AuthenticationPrincipal JwtUserDetails user, Model model){
        model.addAttribute("createPostDto",new CreatePostDto(user.getUsername()));
        return "post";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@AuthenticationPrincipal JwtUserDetails userDetails,
                             @Valid @ModelAttribute CreatePostDto createPostDto,
                             BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "post";
        }
        postService.save(createPostDto, userDetails.getId());
        return "redirect:/";
    }

    @GetMapping("/{postId}")
    public String getDetailPost(@PathVariable Long postId,
                                @RequestParam(value = "isrecommend", required = false, defaultValue = "false") boolean isRecommend,
                                @AuthenticationPrincipal JwtUserDetails user, Model model){

        PostDetailDto dto = postService.getPostDetailDtoById(postId, isRecommend);
        model.addAttribute("header", dto.getHeader());
        model.addAttribute("footer", dto.getCommentList());
        model.addAttribute("currentUserNickname", user != null ? user.getUsername() : null);

        return "post-detail";
    }

    @GetMapping(("/edit/{postId}"))
    @CheckResourceOwner(type = ResourceType.POST)
    public String editPage(@PathVariable Long postId,
                           @AuthenticationPrincipal JwtUserDetails user,
                           Model model) {
        // 게시글 정보 가져오기
        Post post = postService.findById(postId);

        // CreatePostDto 객체 생성 및 값 설정
        CreatePostDto postDto = new CreatePostDto(post.getUser().getNickname());
        postDto.setHead(post.getHead());
        postDto.setContext(post.getContext());

        // 모델에 데이터 추가
        model.addAttribute("postDto", postDto);
        model.addAttribute("postId", postId);

        return "post-edit";
    }

    @PostMapping("/edit/{postId}")
    @CheckResourceOwner(type = ResourceType.POST)
    public String editPost(@PathVariable Long postId,
                           @AuthenticationPrincipal JwtUserDetails user,
                           @Valid @ModelAttribute CreatePostDto postDto,
                           BindingResult bindingResult,
                           Model model) {
        // 유효성 검사 실패 시 수정 페이지로 다시 이동
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", postId);
            return "post-edit";
        }

        Post update = postService.update(postId, postDto);
        return "redirect:/post/" + update.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recommend/{postId}")
    public String recommendPost(@PathVariable Long postId, @AuthenticationPrincipal JwtUserDetails user){
        likeService.recommendPost(postId,user.getId());

        return "redirect:/post/" + postId;
    }


    @DeleteMapping("/{id}")
    @CheckResourceOwner(type = ResourceType.POST)
    @ResponseBody
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails user){
        postService.deletePost(id);
    }
}
