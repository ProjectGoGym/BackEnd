package com.gogym.post.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Wish extends BaseEntity {

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @JoinColumn(name = "post_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Post post;
}
