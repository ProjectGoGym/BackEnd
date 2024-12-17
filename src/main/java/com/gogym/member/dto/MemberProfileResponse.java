package com.gogym.member.dto;

public record MemberProfileResponse(
    Long memberId,
    String email, 
    String name, 
    String nickname,
    String phone,
    String profileImageUrl) {
}

