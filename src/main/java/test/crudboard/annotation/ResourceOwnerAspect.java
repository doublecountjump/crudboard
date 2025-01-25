package test.crudboard.annotation;


import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import test.crudboard.provider.local.LocalUserDetails;
import test.crudboard.repository.JpaPostRepository;
import test.crudboard.service.CommentService;
import test.crudboard.service.PostService;

@Aspect
@Component
@RequiredArgsConstructor
public class ResourceOwnerAspect {
    private final PostService postService;
    private final CommentService commentService;

    @Around("@annotation(checkResourceOwner)")
    public Object checkOwner(ProceedingJoinPoint pjp, CheckResourceOwner checkResourceOwner) throws Throwable{
        Object[] args = pjp.getArgs();
        Long resourceId = (Long) args[0];
        Object user = args[1];

        boolean hasPermission = switch (checkResourceOwner.type()){
            case POST -> checkPostOwner(resourceId, user);
            case COMMENT -> checkCommentOwner(resourceId, user);
        };

        if (!hasPermission) {
            throw new AuthenticationCredentialsNotFoundException("권한이 없습니다");
        }

        return pjp.proceed();

    }

    private boolean checkPostOwner(Long resourceId, Object user) {
        if(user instanceof OAuth2User oAuth2User){
            return postService.isPostOwner(resourceId, oAuth2User.getName());
        } else if (user instanceof LocalUserDetails userDetails) {
            return postService.isPostOwner(resourceId, userDetails.getUsername());
        }else return false;
    }

    private boolean checkCommentOwner(Long resourceId, Object user) {
        if(user instanceof OAuth2User oAuth2User){
            return commentService.isCommentOwner(resourceId, oAuth2User.getName());
        } else if (user instanceof LocalUserDetails userDetails) {
            return commentService.isCommentOwner(resourceId, userDetails.getUsername());
        }else return false;
    }
}
