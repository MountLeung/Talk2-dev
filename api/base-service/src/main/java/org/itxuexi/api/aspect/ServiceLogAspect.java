package org.itxuexi.api.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Aspect
public class ServiceLogAspect {

    @Around("execution(* org.itxuexi.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {

        long begin = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();
        String pointName = joinPoint.getClass().getName()
                            + "."
                            + joinPoint.getSignature().getName();
        long end = System.currentTimeMillis();
        long gap = end - begin;

        if (gap > 2000) {
            log.error("执行位置{}，执行时间过长，耗费了{}毫秒", pointName, gap);
        } else if (gap > 1000) {
            log.warn("执行位置{}，执行时间略长，耗费了{}毫秒", pointName, gap);
        } else {
            log.info("执行位置{}，执行时间正常，耗费了{}毫秒", pointName, gap);
        }
        return proceed;
    }
}
