package com.gogym.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Pointcut("execution(* com.gogym..controller..*(..))")
  public void apiLayer() {}

  @Pointcut("execution(* com.gogym..service..*(..))")
  public void serviceLayer() {}

  @Around("apiLayer()")
  public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().toShortString();
    log.info("▶ API 호출 시작: {}", methodName);

    long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long endTime = System.currentTimeMillis();

    log.info("◀ API 호출 종료: {}, 실행 시간: {} ms", methodName, endTime - startTime);
    return result;
  }

  @Around("serviceLayer()")
  public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().toShortString();
    log.info("▶ 서비스 메서드 시작: {}", methodName);

    long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long endTime = System.currentTimeMillis();

    log.info("◀ 서비스 메서드 종료: {}, 실행 시간: {} ms", methodName, endTime - startTime);
    return result;
  }
}