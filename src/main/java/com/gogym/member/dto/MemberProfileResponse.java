package com.gogym.member.dto;

public record MemberProfileResponse(String email, String name, String nickname, String phone,
    String profileImageUrl) {
}

