package com.gogym.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Pointcut("execution(* com.gogym..controller..*(..))")
  public void apiLayer() {
  }

  @Pointcut("execution(* com.gogym..service..*(..)) && !execution(* com.gogym..schedule..*(..)) && !execution(* com.gogym.notification.service.NotificationService.getEmitters(..))")
  public void serviceLayer() {
  }

  @Around("apiLayer()")
  public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
    String transactionId = createTransactionId();
    HttpServletRequest originalRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(originalRequest);

    Map<String, Object> params = new HashMap<>();
    try {
      params.putAll(getParams(requestWrapper));

      String requestBody = getRequestBody(requestWrapper);
      if (requestBody != null && !requestBody.isEmpty()) {
        params.putAll(parseBodyToMap(requestBody));
      }
    } catch (Exception e) {
      log.error("LoggerAspect error", e);
    }

    log.info("\n[{}] ▶ [Controller] {}.{} 호출 시작\n📍 HTTP Method: {}, URI: {}\n📍 Params: {}\n",
        transactionId,
        joinPoint.getSignature().getDeclaringType().getSimpleName(),
        joinPoint.getSignature().getName(),
        requestWrapper.getMethod(),
        requestWrapper.getRequestURI(),
        params);

    Object result = joinPoint.proceed();

    return result;
  }

  @Around("serviceLayer()")
  public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
    String transactionId = getTransactionId();
    String methodName = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();

    log.info("\n[{}] ▶ [Service] {} 호출 시작", transactionId, methodName);
    log.info("📍 매개변수: {}\n", args);

    long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long endTime = System.currentTimeMillis();

    log.info("\n[{}] ◀ [Service] {} 호출 종료, 실행 시간: {} ms\n", transactionId, methodName,
        endTime - startTime);

    return result;
  }

  private Map<String, Object> getParams(HttpServletRequest request) {
    Map<String, Object> params = new HashMap<>();
    Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String param = paramNames.nextElement();
      String value = request.getParameter(param);
      if ("password".equalsIgnoreCase(param) || "newPassword".equalsIgnoreCase(param)) {
        value = "*****";
      }
      params.put(param, value);
    }
    return params;
  }

  private String getRequestBody(ContentCachingRequestWrapper request) {
    try {
      byte[] content = request.getContentAsByteArray();
      if (content.length > 0) {
        return new String(content, StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      log.error("요청 바디 읽기 중 오류 발생", e);
    }
    return null;
  }

  private Map<String, Object> parseBodyToMap(String body) {
    Map<String, Object> bodyParams = new HashMap<>();
    try {
      String[] pairs = body.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=");
        if (keyValue.length == 2) {
          String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
          String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
          if ("password".equalsIgnoreCase(key) || "newPassword".equalsIgnoreCase(key)) {
            value = "*****";
          }
          bodyParams.put(key, value);
        }
      }
    } catch (Exception e) {
      log.error("요청 바디 파싱 중 오류 발생", e);
    }
    return bodyParams;
  }

  private String createTransactionId() {
    String transactionId = UUID.randomUUID().toString().substring(0, 8);
    RequestContextHolder.getRequestAttributes()
        .setAttribute("transactionId", transactionId, RequestAttributes.SCOPE_REQUEST);
    return transactionId;
  }

  private String getTransactionId() {
    Object transactionId = RequestContextHolder.getRequestAttributes()
        .getAttribute("transactionId", RequestAttributes.SCOPE_REQUEST);
    return transactionId != null ? transactionId.toString() : UUID.randomUUID().toString();
  }
}