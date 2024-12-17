package com.gogym.member.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoProfileResponse( 
    Long id,
    String connectedAt,
    KakaoAccount kakaoAccount
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record KakaoAccount(
        String email,
        Boolean hasEmail,
        Boolean emailNeedsAgreement
    ) {}
}


