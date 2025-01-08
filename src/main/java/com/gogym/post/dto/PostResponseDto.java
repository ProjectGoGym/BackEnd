package com.gogym.post.dto;

import com.gogym.post.entity.Post;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import com.gogym.region.dto.RegionResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PostResponseDto(

    Long postId,
    Long authorId,
    String authorNickname,
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
    String district,
    LocalDateTime createdAt,
    boolean isWished

) {

  public static PostResponseDto fromEntity(Post post, RegionResponseDto regionResponseDto, boolean isWished) {

    return PostResponseDto.builder()
        .postId(post.getId())
        .authorId(post.getAuthor().getId())
        .authorNickname(post.getAuthor().getNickname())
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
        .createdAt(post.getCreatedAt())
        .isWished(isWished)
        .build();
  }
}