package com.gogym.aop;

import com.gogym.common.annotation.RedissonLock;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

  private final RedissonClient redissonClient;

  @Around("@annotation(com.gogym.common.annotation.RedissonLock)")
  public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);

    String[] parameterNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();
    String lockKey = (String) RedisLockSpELParser.getLockKey(parameterNames, args, redissonLock.key());

    RLock lock = redissonClient.getLock(lockKey);
    boolean acquired = false;

    try {
      acquired = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), java.util.concurrent.TimeUnit.SECONDS);
      if (!acquired) {
        log.warn("락 획득 실패, lockKey: {}", lockKey);
//        throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
      }

      log.info("락 획득 성공, lockKey: {}", lockKey);
      return joinPoint.proceed();

    } catch (Exception e) {
      log.error("🚫 예외 발생 중 - lockKey: {}, 예외: {}", lockKey, e);
      throw e;
    } finally {
      if (lock.isHeldByCurrentThread()) {
        try {
          lock.unlock();
          log.info("🔓 락 해제 - lockKey: {}", lockKey);
        } catch (Exception e) {
          log.error("🚫 락 해제 중 예외 발생 - lockKey: {}", lockKey, e);
        }
      } else {
        log.warn("🚫 락 소유자가 아님 - lockKey: {}", lockKey);
      }
    }
  }
}