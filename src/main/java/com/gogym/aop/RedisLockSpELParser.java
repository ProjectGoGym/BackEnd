package com.gogym.aop;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Slf4j
public final class RedisLockSpELParser {

  public static Object getLockKey(String[] parameterNames, Object[] args, String key) {
    try {
      SpelExpressionParser parser = new SpelExpressionParser();
      StandardEvaluationContext context = new StandardEvaluationContext();

      for (int i = 0; i < parameterNames.length; i++) {
        context.setVariable(parameterNames[i], args[i]);
      }

      Object result = parser.parseExpression(key).getValue(context, Object.class);

      if (result == null) {
        log.error("🔴 스프링 EL 구문이 올바르게 해석되지 않았습니다: key = {}", key);
        throw new CustomException(ErrorCode.LOCK_KEY_NOT_FOUND);
      }

      return result;
    } catch (Exception e) {
      throw new CustomException(ErrorCode.LOCK_KEY_NOT_FOUND, e.getMessage());
    }
  }
}