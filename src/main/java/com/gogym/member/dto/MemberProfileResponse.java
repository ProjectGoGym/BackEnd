package com.gogym.member.dto;

public record MemberProfileResponse(
    Long memberId,
    String email,
    String name,
    String nickname,
    String phone,
    String profileImageUrl,
    long gymPayBalance,
    Long gymPayId,
    Long regionId1,
    Long regionId2,
    String regionName1, // 부모 지역 노드
    String regionName2  // 자식 지역 노드
    ) {
}

