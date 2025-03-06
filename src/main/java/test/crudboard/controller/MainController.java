package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import test.crudboard.entity.dto.TitleDto;
import test.crudboard.entity.dto.UserInfoDto;
import test.crudboard.entity.dto.UserJoinDto;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;


@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final PostService postService;

    @GetMapping(value = {"","/","/{page}"})
    public String home(@AuthenticationPrincipal JwtUserDetails userDetails, Model model,
                       @PathVariable(required = false) Integer page){
        if(page == null){
            page = 1;
        }

        if(userDetails != null){
            System.out.println("user id : " + userDetails.getUsername());
            model.addAttribute("username", userDetails.getUsername());
        }

        Page<TitleDto> titleList = postService.getTitleList(page);
        model.addAttribute("titleList",titleList);

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
}
