package test.crudboard.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.entity.post.dto.SearchRequestDto;

import test.crudboard.domain.entity.user.dto.UserInfoDto;
import test.crudboard.domain.entity.user.dto.UserJoinDto;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.service.PostHeaderService;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final PostService postService;
    private final JpaUserRepository userRepository;
    private final PostHeaderService postHeaderService;

    private final int PAGE_SIZE = 10;
    @GetMapping(value = {"","/","/{page}"})
    public String home(@AuthenticationPrincipal JwtUserDetails userDetails, Model model,
                       @RequestParam(value = "isrecommend", required = false, defaultValue = "false") boolean isRecommend,
                       @PathVariable(required = false) Integer page){

        //페이지가 지정되지 않으면, 1페이지로 고정
        if(page == null){
            page = 1;
        }

        //로그인한 사용자의 경우, 사용자의 이름을 모델에 담아 전달
        if(userDetails != null){
            model.addAttribute("username", userDetails.getUsername());
        }

        //해당하는 페이지의 게시글 목록 조회
        Page<PostHeaderDto> titleList = postHeaderService.getTitleList(page,isRecommend);

        //해당하는 페이지 블록 설정
        int startPage = getStartPage(page, PAGE_SIZE);
        int endPage = Math.min(startPage + PAGE_SIZE - 1, titleList.getTotalPages());

        //모델에 전달
        model.addAttribute("titleList",titleList);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "main";
    }

    private int getStartPage(int page, int size){
        int currentBlock = (int) Math.ceil((double) page / size);
        return (currentBlock - 1) * size + 1;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/join")
    public String join(){
        return "join";
    }

    @PostMapping("/join")
    public String joinUser(UserJoinDto userJoinDto){
        userService.save(userJoinDto);
        return "redirect:/";
    }


    @GetMapping("/mypage/{userId}")
    @PreAuthorize("isAuthenticated()")
    public String myPage(@AuthenticationPrincipal JwtUserDetails user, Model model){
        UserInfoDto userInfo = userService.getUserInfo(user.getUsername());
        model.addAttribute("userInfo",userInfo);
        return "user-detail";
    }


    @GetMapping("/search")
    public String searchPost(@Valid @ModelAttribute SearchRequestDto search, @AuthenticationPrincipal JwtUserDetails user, Model model){
        int page = search.getPage();
        String text = search.getContent();
        Page<PostHeaderDto> titleDto = switch (search.getType()){
            case HEAD ->  postService.searchPostByHead(text, PageRequest.of(page - 1,5, Sort.by("id").descending()));
            case HEAD_CONTENT ->  postService.searchPostByHeadOrContent(text, PageRequest.of(page - 1,5, Sort.by("id").descending()));
            case NICKNAME ->  postService.searchPostByNickname(text, PageRequest.of(page - 1,5, Sort.by("id").descending()));
            default ->  postService.searchPostByHead(text, PageRequest.of(page - 1,5, Sort.by("id").descending()));
        };

        if(user != null){
            model.addAttribute("username", user.getUsername());
        }
        model.addAttribute("titleList", titleDto);
        return "main";

    }
}
