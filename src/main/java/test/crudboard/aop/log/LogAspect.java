package test.crudboard.aop.log;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import test.crudboard.provider.JwtUserDetails;

@Aspect
@Component
public class LogAspect {

    @Around("@annotation(logExtract)")
    public Object extractLog(ProceedingJoinPoint joinPoint, LogExtract logExtract) throws Throwable {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof  JwtUserDetails){
            
        }

        return joinPoint.proceed();
    }
}
