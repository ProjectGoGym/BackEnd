package com.gogym.member.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoProfileResponse {
  private Long id;
  private String connectedAt;
  private KakaoAccount kakaoAccount;

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class KakaoAccount {
    private String email;
    private Boolean hasEmail;
    private Boolean emailNeedsAgreement;
  }
}


