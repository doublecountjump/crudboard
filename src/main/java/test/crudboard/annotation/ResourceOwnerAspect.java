package test.crudboard.annotation;


import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.service.CommentService;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;


/**
 * AOP 기능 하나 더 추가해서 써먹어보기
 * EX - 로그인한 사용자들의 로그 추적?
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceOwnerAspect {
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    @Around("@annotation(checkResourceOwner)")
    public Object checkOwner(ProceedingJoinPoint pjp, CheckResourceOwner checkResourceOwner) throws Throwable{
        Object[] args = pjp.getArgs();
        Long resourceId = (Long) args[0];
        Object user = args[1];

        boolean hasPermission = switch (checkResourceOwner.type()){
            case POST -> checkPostOwner(resourceId, user);
            case COMMENT -> checkCommentOwner(resourceId, user);
            //case USER -> checkUser(resourceId, user);
        };

        if (!hasPermission) {
            log.warn("not allowed");
            throw new AuthenticationCredentialsNotFoundException("권한이 없습니다");
        }

        return pjp.proceed();

    }

    private boolean checkPostOwner(Long resourceId, Object user) {
        if(user instanceof JwtUserDetails User){
            return postService.isPostOwner(resourceId, User.getUsername());
        }else return false;
    }

    private boolean checkCommentOwner(Long resourceId, Object user) {
        if(user instanceof JwtUserDetails User){
            return commentService.isCommentOwner(resourceId, User.getUsername());
        }else return false;
    }

    //필요한가?
    @Deprecated
    private boolean checkUser(Long resourceId, Object user){
        if(user instanceof JwtUserDetails User){
            return userService.isIdentification(resourceId, User.getUsername());
        }else return false;
    }
}
