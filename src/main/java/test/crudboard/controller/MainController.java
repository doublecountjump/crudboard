package test.crudboard.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import test.crudboard.annotation.CheckResourceOwner;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.TitleDto;
import test.crudboard.entity.dto.UserInfoDto;
import test.crudboard.entity.dto.UserJoinDto;
import test.crudboard.entity.enumtype.ResourceType;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final PostService postService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Object authentication, Model model){
        String userEmail = getUserEmail(authentication);
        if(userEmail != null){
            log.info("UserEmail: {}", userEmail);
            User user = userService.findUserByEmail(userEmail);

            UserInfoDto userInfoDto = UserInfoDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .avatar_url(user.getProfileImage())
                    .build();

            model.addAttribute("user", userInfoDto);
        }else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("Current Authentication: {}", auth);
            if(auth != null) {
                log.info("Authentication Principal type: {}", auth.getPrincipal().getClass().getName());
            }
        }
        List<TitleDto> titleList = postService.getTitleList();

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
    @CheckResourceOwner(type = ResourceType.USER)
    public String myPage(@PathVariable Long userId, @AuthenticationPrincipal Object user, Model model){
        UserInfoDto userInfo = userService.getUserInfo(getUserEmail(user));

        model.addAttribute("userInfo",userInfo);
        return "user-detail";
    }
    private String getUserEmail(Object user) {
        if (user instanceof OAuth2User auth2User) {
            return auth2User.getName();
        } else if (user instanceof LocalUserDetails localUser) {
            return localUser.getUsername();
        }
        else return null;
    }
}
