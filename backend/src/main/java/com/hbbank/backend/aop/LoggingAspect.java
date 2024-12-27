package com.hbbank.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 모든 컨트롤러 메서드에 적용
    @Around("execution(* com.hbbank.backend.controller.*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 요청 로깅
        log.info("[{}] 요청: {}.{} - 파라미터: {}",
                requestId,
                className,
                methodName,
                Arrays.toString(joinPoint.getArgs())
        );

        long startTime = System.currentTimeMillis();

        try {
            // 메서드 실행
            Object result = joinPoint.proceed();

            // 응답 로깅
            log.info("[{}] 응답: {}.{} - 소요시간: {}ms",
                    requestId,
                    className,
                    methodName,
                    System.currentTimeMillis() - startTime
            );

            return result;

        } catch (Exception e) {
            // 에러 로깅
            log.error("[{}] 에러: {}.{} - 예외: {}",
                    requestId,
                    className,
                    methodName,
                    e.getMessage()
            );
            throw e;
        }
    }

    // 모든 서비스 메서드에 적용
    @Around("execution(* com.hbbank.backend.service.*.*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 서비스 시작 로깅
        log.debug("서비스 시작: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();

            // 서비스 종료 로깅
            log.debug("서비스 종료: {}.{}", className, methodName);

            return result;

        } catch (Exception e) {
            // 서비스 에러 로깅
            log.error("서비스 에러: {}.{} - {}",
                    className,
                    methodName,
                    e.getMessage()
            );
            throw e;
        }
    }
}