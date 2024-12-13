package com.gogym.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoProfileResponse {
  private Long id;

  @JsonProperty("connected_at")
  private String connectedAt;

  @JsonProperty("kakao_account")
  private KakaoAccount kakaoAccount;

  @Getter
  @NoArgsConstructor
  public static class KakaoAccount {
    private String email;

    @JsonProperty("has_email")
    private Boolean hasEmail;

    @JsonProperty("email_needs_agreement")
    private Boolean emailNeedsAgreement;
  }
}

