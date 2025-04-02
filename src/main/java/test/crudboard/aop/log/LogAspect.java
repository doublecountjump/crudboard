package test.crudboard.aop.log;


import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LogAspect {

    /**
     * 메서드 실행 체크
     * @param joinPoint(ProceedingJoinPoint) 메서드의 실행 흐름을 제어할 수 있는 객체
     * @throws Throwable
     */
    @Around("execution(* test.crudboard.service.*.*(..))")
    public Object extractLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        //proceed() 호출 시 메서드 실행, 호출 안하면 메서드 실행이 안된다!
        Object proceed = joinPoint.proceed();
        long end = System.currentTimeMillis() - start;

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        log.info("[성능] {}.{} - {}ms", className, methodName, end);

        return proceed;
    }
}
