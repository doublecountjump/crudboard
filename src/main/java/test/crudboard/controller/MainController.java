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
import test.crudboard.entity.Comment;
import test.crudboard.entity.dto.SearchRequestDto;
import test.crudboard.entity.dto.MainTitleDto;
import test.crudboard.entity.dto.UserInfoDto;
import test.crudboard.entity.dto.UserJoinDto;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;

import java.util.List;
import java.util.Objects;


@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final PostService postService;
    private final JpaUserRepository userRepository;
    @GetMapping(value = {"","/","/{page}"})
    public String home(@AuthenticationPrincipal JwtUserDetails userDetails, Model model,
                       @PathVariable(required = false) Integer page){
        if(page == null){
            page = 1;
        }

        if(userDetails != null){
            model.addAttribute("username", userDetails.getUsername());
        }
        Page<MainTitleDto> titleList = postService.getTitleList(page);

        int totalPages = titleList.getTotalPages();
        int pageSize = 10; // 한 블록에 보여줄 페이지 수
        int currentBlock = (int) Math.ceil((double) page / pageSize);
        int startPage = (currentBlock - 1) * pageSize + 1;
        int endPage = Math.min(startPage + pageSize - 1, totalPages);

        model.addAttribute("titleList",titleList);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "main";
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
        Page<MainTitleDto> titleDto = switch (search.getType()){
            case HEAD ->  postService.searchPostByHead(text, PageRequest.of(page - 1,5, Sort.by("created").descending()));
            case HEAD_CONTENT ->  postService.searchPostByHeadOrContent(text, PageRequest.of(page - 1,5, Sort.by("created").descending()));
            case NICKNAME ->  postService.searchPostByNickname(text, PageRequest.of(page - 1,5, Sort.by("created").descending()));
            default ->  postService.searchPostByHead(text, PageRequest.of(page - 1,5, Sort.by("created").descending()));
        };

        if(user != null){
            model.addAttribute("username", user.getUsername());
        }
        model.addAttribute("titleList", titleDto);
        return "main";

    }
}
