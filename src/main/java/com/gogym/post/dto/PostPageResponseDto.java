package com.gogym.post.dto;

import com.gogym.post.entity.Post;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PostPageResponseDto(

    Long postId,
    String title,
    PostStatus status,
    Long amount,
    String imageUrl1,
    String imageUrl2,
    String imageUrl3,
    String gymName,
    LocalDateTime createdAt,
    Long wishCount,
    String authorNickname,
    PostType postType

) {

  @QueryProjection
  public PostPageResponseDto {
  }

  public static PostPageResponseDto fromEntity(Post post) {

    return PostPageResponseDto.builder()
        .postId(post.getId())
        .title(post.getTitle())
        .status(post.getStatus())
        .amount(post.getAmount())
        .imageUrl1(post.getImageUrl1())
        .imageUrl2(post.getImageUrl2())
        .imageUrl3(post.getImageUrl3())
        .gymName(post.getGym().getGymName())
        .createdAt(post.getCreatedAt())
        .wishCount(post.getWishCount())
        .authorNickname(post.getAuthor().getNickname())
        .postType(post.getPostType())
        .build();
  }
}