package test.crudboard.aop.annotation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import test.crudboard.provider.JwtUserDetails;
import test.crudboard.service.CommentService;
import test.crudboard.service.PostService;
import test.crudboard.service.UserService;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceOwnerAspect {
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    /**
     * 본인이 작성한 객체인지 검증.
     * @param pjp 대상 메서드의 파라미터
     * @param checkResourceOwner 검증하고자 하는 객체
     * @return
     * @throws Throwable
     */
    @Around("@annotation(checkResourceOwner)")
    public Object checkOwner(ProceedingJoinPoint pjp, CheckResourceOwner checkResourceOwner) throws Throwable{
        Object[] args = pjp.getArgs();
        Long resourceId = (Long) args[0];
        Object user = args[1];

        //권한 검증, 본인이 작성했다면 true
        boolean hasPermission = switch (checkResourceOwner.type()){
            case POST -> checkPostOwner(resourceId, user);
            case COMMENT -> checkCommentOwner(resourceId, user);
            //case USER -> checkUser(resourceId, user);
        };

        //권한이 없다면
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
