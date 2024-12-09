package com.gogym.member.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRequest(
    @NotBlank String name, 
    @NotBlank String nickname,
    @NotBlank String phone, 
    String profileImageUrl) {
}
