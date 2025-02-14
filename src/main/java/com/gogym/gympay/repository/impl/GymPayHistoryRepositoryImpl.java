package com.gogym.gympay.repository.impl;

import static com.gogym.gympay.entity.QGymPayHistory.gymPayHistory;
import static com.gogym.member.entity.QMember.member;
import static com.gogym.post.entity.QPost.post;

import com.gogym.gympay.dto.response.GetHistory;
import com.gogym.gympay.dto.response.QGetHistory;
import com.gogym.gympay.repository.GymPayHistoryRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class GymPayHistoryRepositoryImpl implements GymPayHistoryRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<GetHistory> getAllHistoriesByGymPayIdAndPeriod(Long memberId, LocalDateTime startDate,
      LocalDateTime endDate, Pageable pageable) {
    var query = queryFactory.select(new QGetHistory(
            gymPayHistory.id,
            gymPayHistory.amount,
            gymPayHistory.balance,
            gymPayHistory.transferType,
            gymPayHistory.counterpartyId,
            member.nickname,
            post.id,
            post.title,
            gymPayHistory.createdAt
        ))
        .from(gymPayHistory)
        .leftJoin(member).on(gymPayHistory.counterpartyId.eq(member.id))
        .leftJoin(post).on(gymPayHistory.postId.eq(post.id))
        .where(gymPayHistory.gymPay.member.id.eq(memberId));

    if (startDate != null && endDate != null) {
      query.where(gymPayHistory.createdAt.between(startDate, endDate));
    }

    List<GetHistory> histories = query
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory.select(gymPayHistory.count())
        .from(gymPayHistory)
        .where(gymPayHistory.gymPay.member.id.eq(memberId))
        .fetchOne();

    return new PageImpl<>(histories, pageable, total);
  }
}
