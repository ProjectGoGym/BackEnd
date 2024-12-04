package com.gogym.common.annotation;

import com.gogym.aop.LoginMemberId;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

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
    HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
    String token = jwtTokenProvider.resolveOrExtractToken(request, null);

    if (!jwtTokenProvider.validateToken(token)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    Authentication authentication = jwtTokenProvider.getAuthentication(token);
    if (authentication == null || authentication.getName() == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    return Long.valueOf(authentication.getName());
  }
}
