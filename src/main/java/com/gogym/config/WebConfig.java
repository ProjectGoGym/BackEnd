package com.gogym.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.gogym.aop.LoginMemberIdHandler;
import java.util.List;

// 어노테이션을 스프링에 등록하는 클래스

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginMemberIdHandler memberIdResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(memberIdResolver);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // 모든 엔드포인트에 대해 CORS 설정
        .allowedOrigins( // 프론트엔드 주소
            "http://localhost:3001", "http://localhost:3000", "https://gogym-eight.vercel.app/")
        .allowedMethods("*") // 허용할 HTTP 메서드
        .allowedHeaders("*") // 모든 헤더 허용
        //.exposedHeaders("*")  // 모든 헤더 노출
        .exposedHeaders("Authorization", "Content-Type", "X-Requested-With") // 명시적으로 헤더 노출 시도
        .allowCredentials(true); // 자격증명(쿠키, 인증 헤더 등)을 포함하는 요청 허용
  }
}
