package com.gogym.post.entity;

import static com.gogym.post.type.PostStatus.POSTING;

import com.gogym.common.entity.BaseEntity;
import com.gogym.member.entity.Member;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @JoinColumn(name = "gym_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Gym gym;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(name = "post_type", nullable = false)
  private PostType postType;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PostStatus status;

  @Column(nullable = false)
  private Long amount;

  @Column(name = "image_url_1")
  private String imageUrl1;

  @Column(name = "image_url_2")
  private String imageUrl2;

  @Column(name = "image_url_3")
  private String imageUrl3;

  @Column(name = "wish_count")
  private Long wishCount;

  @Column(name = "membership_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private MembershipType membershipType;

  @Column(name = "expiration_date")
  private LocalDate expirationDate;

  @Column(name = "remaining_sessions")
  private Long remainingSessions;

  public static Post of (Member member, Gym gym, PostRequestDto postRequestDto) {

    return Post.builder()
        .member(member)
        .gym(gym)
        .title(postRequestDto.title())
        .content(postRequestDto.content())
        .postType(postRequestDto.postType())
        .status(POSTING)
        .amount(postRequestDto.amount())
        .imageUrl1(postRequestDto.imageUrl1())
        .imageUrl2(postRequestDto.imageUrl2())
        .imageUrl3(postRequestDto.imageUrl3())
        .wishCount(0L)
        .membershipType(postRequestDto.membershipType())
        .expirationDate(postRequestDto.expirationDate())
        .remainingSessions(postRequestDto.remainingSessions())
        .build();
  }
}