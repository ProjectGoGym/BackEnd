package com.gogym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean(name = "portOneClient")
  public WebClient portOneClient(WebClient.Builder builder) {
    return builder.baseUrl("https://api.portone.io").build();
  }
}