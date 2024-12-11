package com.gogym.post.dto;

import com.gogym.post.entity.Post;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import com.gogym.region.dto.RegionResponseDto;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PostResponseDto(

    Long postId,
    Long memberId,
    String memberNickname,
    String title,
    String content,
    PostType postType,
    PostStatus status,
    Long amount,
    String imageUrl1,
    String imageUrl2,
    String imageUrl3,
    Long wishCount,
    MembershipType membershipType,
    LocalDate expirationDate,
    Long remainingSessions,
    Long gymId,
    String gymName,
    String gymKakaoUrl,
    String city,
    String district

) {

  public static PostResponseDto fromEntity(Post post, RegionResponseDto regionResponseDto) {

    return PostResponseDto.builder()
        .postId(post.getId())
        .memberId(post.getMember().getId())
        .memberNickname(post.getMember().getNickname())
        .title(post.getTitle())
        .content(post.getContent())
        .postType(post.getPostType())
        .status(post.getStatus())
        .amount(post.getAmount())
        .imageUrl1(post.getImageUrl1())
        .imageUrl2(post.getImageUrl2())
        .imageUrl3(post.getImageUrl3())
        .wishCount(post.getWishCount())
        .membershipType(post.getMembershipType())
        .expirationDate(post.getExpirationDate())
        .remainingSessions(post.getRemainingSessions())
        .gymId(post.getGym().getId())
        .gymName(post.getGym().getGymName())
        .gymKakaoUrl(post.getGym().getGymKakaoUrl())
        .city(regionResponseDto.city())
        .district(regionResponseDto.district())
        .build();
  }
}