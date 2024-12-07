package com.gogym.aop;

import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.common.annotation.LoginMemberId;
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
    String token = jwtTokenProvider.extractToken(request);

    if (!jwtTokenProvider.validateToken(token)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
      
    }
    
    // JWT에서 memberId 추출
    Long memberId = jwtTokenProvider.extractMemberId(token);
    if (memberId == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
      
    }
    
    return memberId;
  }
}
