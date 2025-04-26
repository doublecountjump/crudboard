package test.crudboard.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import test.crudboard.domain.aop.valid.CheckResourceOwner;
import test.crudboard.domain.aop.valid.ResourceType;
import test.crudboard.domain.post.entity.Post;
import test.crudboard.domain.post.dto.CreatePostDto;
import test.crudboard.domain.post.dto.PostDetailDto;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.LikeService;
import test.crudboard.service.image.ImageService;
import test.crudboard.domain.post.service.PostRequestService;
import test.crudboard.domain.post.service.PostService;
import test.crudboard.service.image.FileService;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final LikeService likeService;
    private final PostRequestService postRequestService;
    private final FileService fileService;
    private final ImageService imageService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(@AuthenticationPrincipal JwtUserDetails user, Model model){
        //게시글 작성칸, 작성자를 표시하기 위해 현재 로그인한 사용자의 이름을 모델에 반환
        model.addAttribute("createPostDto",new CreatePostDto(user.getUsername()));
        return "post";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@AuthenticationPrincipal JwtUserDetails userDetails,
                             @Valid @ModelAttribute CreatePostDto createPostDto,
                             BindingResult bindingResult){
        //유효성 검사 실패 시 수정 페이지로 다시 이동
        if(bindingResult.hasErrors()){
            return "post";
        }

        // 이미지 업로드 처리
        List<String> imageUrls = new ArrayList<>();
        if (createPostDto.getImages() != null && !createPostDto.getImages().isEmpty()) {
            for (MultipartFile image : createPostDto.getImages()) {
                if (!image.isEmpty()) {
                    String imageUrl = fileService.uploadFile(image);
                    imageUrls.add(imageUrl);
                }
            }
        }

        // 게시글 저장 시 이미지 URL도 함께 저장
        Post save = postService.save(createPostDto, userDetails.getId());
        imageService.save(imageUrls, save.getId());

        return "redirect:/";
    }

    @GetMapping("/detail/{postId}")
    public String getDetailPost(@PathVariable Long postId,
                                @RequestParam(value = "isrecommend", required = false, defaultValue = "false") boolean isRecommend,
                                @AuthenticationPrincipal JwtUserDetails user, Model model){

        //게시글, 댓글 부분 각각 불러옴
        PostDetailDto dto = postRequestService.getPostDetailDtoById(postId, isRecommend);
        List<String> image = imageService.getImage(postId);
        model.addAttribute("header", dto.getHeader());
        model.addAttribute("footer", dto.getCommentList());
        model.addAttribute("images", image);
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
        return "redirect:/post/detail/" + update.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/recommend/{postId}")
    public String recommendPost(@PathVariable Long postId, @AuthenticationPrincipal JwtUserDetails user){
        //추천, 이미 추천했다면 적용되지 않음
        likeService.recommendPost(postId,user.getId());
        return "redirect:/post/detail/" + postId;
    }

    @DeleteMapping("/{id}")
    @CheckResourceOwner(type = ResourceType.POST)
    @ResponseBody
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails user){
        //게시글 삭제
        postService.deletePost(id);
    }
}