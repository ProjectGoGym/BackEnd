package com.gogym.post.dto;

import com.gogym.post.entity.Post;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PostResponseDto(

    String title,
    String content,
    PostType postType,
    PostStatus status,
    Long amount,
    String imageUrl1,
    String imageUrl2,
    String imageUrl3,
    String gymName,
    Long wishCount,
    MembershipType membershipType,
    LocalDate expirationDate,
    Long remainingSessions,
    String gymKakaoUrl

) {

  public static PostResponseDto fromEntity(Post post) {

    return PostResponseDto.builder()
        .title(post.getTitle())
        .content(post.getContent())
        .postType(post.getPostType())
        .status(post.getStatus())
        .amount(post.getAmount())
        .imageUrl1(post.getImageUrl1())
        .imageUrl2(post.getImageUrl2())
        .imageUrl3(post.getImageUrl3())
        .gymName(post.getGym().getGymName())
        .wishCount(post.getWishCount())
        .membershipType(post.getMembershipType())
        .expirationDate(post.getExpirationDate())
        .remainingSessions(post.getRemainingSessions())
        .gymKakaoUrl(post.getGym().getGymKakaoUrl())
        .build();
  }
}