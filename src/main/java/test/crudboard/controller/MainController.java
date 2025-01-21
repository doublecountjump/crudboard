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
import org.springframework.web.bind.annotation.PostMapping;
import test.crudboard.entity.User;
import test.crudboard.entity.dto.TitleDto;
import test.crudboard.entity.dto.UserDto;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.repository.JpaUserRepository;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final JpaUserRepository userRepository;
    private final UserService userService;
    private final PostService postService;
    @GetMapping("/")
    public String home(@AuthenticationPrincipal Object user, Model model){
        if(user instanceof OAuth2User){
            OAuth2User auth2User = (OAuth2User) user;
            String name = auth2User.getName();
            User authUser = userRepository.findUserByGithubId(name).get();
            log.info("name : {}", name);
            model.addAttribute(authUser);
        } else if (user instanceof UserDetails) {
            LocalUserDetails userDetails = (LocalUserDetails) user;
            log.info("LocalName : {}",userDetails.getUsername());
            model.addAttribute("user", user);
        }else {
            log.info("User is neither OAuth2User nor UserDetails");
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
    public String joinUser(UserDto userDto){
        userService.save(userDto);
        return "redirect:/";
    }
}
