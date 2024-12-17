package com.gogym.aop;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// @LoginMemberId 어노테이션을 처리하는 코드

@Component
@RequiredArgsConstructor
public class LoginMemberIdHandler implements HandlerMethodArgumentResolver {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoginMemberId.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

    LoginMemberId annotation = parameter.getParameterAnnotation(LoginMemberId.class);
    boolean isRequired = annotation.required();

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      Object principal = auth.getPrincipal();
      if (principal instanceof Long) {
        return principal;
      }
    }

    HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
    String token = jwtTokenProvider.extractToken(request);

    if (token == null || token.isEmpty()) {
      if (isRequired) {
        throw new CustomException(ErrorCode.UNAUTHORIZED);
      }
      return null;
    }

    if (!jwtTokenProvider.validateToken(token)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);

    }

    // JWT에서 memberId 추출
    Long memberId = jwtTokenProvider.extractMemberId(token);
    if (memberId == null) {
      if (isRequired) {
        throw new CustomException(ErrorCode.UNAUTHORIZED);
      }
      return null;
    }

    return memberId;
  }
}
