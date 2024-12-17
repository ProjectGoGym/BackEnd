package com.gogym.post.filter;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import com.gogym.post.dto.PostFilterRequestDto;
import com.gogym.post.entity.QPost;
import com.gogym.post.type.FilterMonthsType;
import com.gogym.post.type.FilterPtType;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class PostFilterBuilder {

  public BooleanBuilder builderFilters(List<Long> regionIds,
      PostFilterRequestDto postFilterRequestDto) {

    BooleanBuilder builder = new BooleanBuilder();
    QPost post = QPost.post;

    builder.and(applyStatusFilter(postFilterRequestDto.status(), post));
    builder.and(applyPostTypeFilter(postFilterRequestDto.postType(), post));
    builder.and(applyMembershipTypeFilter(postFilterRequestDto.membershipType(), post));
    builder.and(applyMonthsTypeFilter(postFilterRequestDto.monthsType(), post));
    builder.and(applyPtTypeFilter(postFilterRequestDto.ptType(), post));
    builder.and(applyRegionFilter(regionIds, post));

    return builder;
  }

  // 게시글 상태 필터
  private Predicate applyStatusFilter(PostStatus status, QPost post) {

    return status != null ? new BooleanBuilder(post.status.eq(status)) : new BooleanBuilder();
  }

  // 게시글 타입 필터
  private Predicate applyPostTypeFilter(PostType postType, QPost post) {

    return postType != null ? new BooleanBuilder(post.postType.eq(postType)) : new BooleanBuilder();
  }

  // 멤버쉽 타입 필터
  private Predicate applyMembershipTypeFilter(MembershipType membershipType, QPost post) {

    return membershipType != null ? new BooleanBuilder(post.membershipType.eq(membershipType))
        : new BooleanBuilder();
  }

  // 남은 기간 필터
  private Predicate applyMonthsTypeFilter(FilterMonthsType monthsType, QPost post) {

    if (monthsType == null) {
      return new BooleanBuilder();
    }

    LocalDate now = LocalDate.now();

    return switch (monthsType) {
      case MONTHS_0_3 -> new BooleanBuilder(post.expirationDate.before(now.plusMonths(3)));
      case MONTHS_3_6 -> new BooleanBuilder(post.expirationDate.between(now.plusMonths(3),
          LocalDate.now().plusMonths(6)));
      case MONTHS_6_PLUS -> new BooleanBuilder(post.expirationDate.after(now.plusMonths(6)));
    };
  }

  // 남은 PT 횟수 필터
  private Predicate applyPtTypeFilter(FilterPtType ptType, QPost post) {

    if (ptType == null) {
      return new BooleanBuilder();
    }

    return switch (ptType) {
      case PT_0_10 -> new BooleanBuilder((post.remainingSessions.loe(10L)));
      case PT_10_25 -> new BooleanBuilder(post.remainingSessions.between(10L, 25L));
      case PT_25_PLUS -> new BooleanBuilder(post.remainingSessions.goe(25L));
    };
  }

  // 관심지역 필터
  private Predicate applyRegionFilter(List<Long> regionIds, QPost post) {

    return regionIds != null && !regionIds.isEmpty() ? new BooleanBuilder(
        post.gym.regionId.in(regionIds)) : new BooleanBuilder();
  }
}