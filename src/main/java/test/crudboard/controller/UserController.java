package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import test.crudboard.domain.entity.user.dto.UserInfoDto;
import test.crudboard.domain.entity.user.dto.UserJoinDto;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.UserService;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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
