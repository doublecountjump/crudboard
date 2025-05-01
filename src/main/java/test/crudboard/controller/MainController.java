package test.crudboard.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.entity.post.dto.SearchRequestDto;
import test.crudboard.security.provider.JwtUserDetails;
import test.crudboard.service.post.PostRequestService;
import test.crudboard.service.post.PostService;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final PostService postService;
    private final PostRequestService postRequestService;

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
        Page<PostHeaderDto> titleList = postRequestService.getTitleList(page,isRecommend);

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

    @GetMapping("/search")
    public String searchPost(@Valid @ModelAttribute SearchRequestDto search,
                             @AuthenticationPrincipal JwtUserDetails user, Model model){
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
