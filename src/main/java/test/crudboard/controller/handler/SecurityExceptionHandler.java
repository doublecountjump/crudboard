package test.crudboard.controller.handler;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import test.crudboard.domain.error.TokenExpiredException;

@ControllerAdvice
public class SecurityExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요");
        return "redirect:/login";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String entityNotFoundExceptionHandler(EntityNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 객체입니다.");
        return "redirect:/";
    }

    @ExceptionHandler(BadCredentialsException.class)
    public String badCredentialsExceptionHandler(BadCredentialsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "BAD");
        return "redirect:/";
    }

    @ExceptionHandler(TokenExpiredException.class)
    public String TokenExpiredException(TokenExpiredException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("tokenExpired",e.getMessage());
        return "redirect:/";
    }
}
