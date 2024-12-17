package com.gogym.config;

import io.portone.sdk.server.webhook.WebhookVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookVerifierConfig {

  @Value("${port-one.webhook.secret}")
  private String secretKey;

  @Bean
  public WebhookVerifier webhookVerifier() {
    return new WebhookVerifier(secretKey);
  }
}
