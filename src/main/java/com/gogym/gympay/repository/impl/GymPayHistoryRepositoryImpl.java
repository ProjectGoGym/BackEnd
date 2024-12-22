package com.gogym.gympay.repository.impl;

import static com.gogym.gympay.entity.QGymPayHistory.gymPayHistory;
import static com.gogym.member.entity.QMember.member;
import static com.gogym.post.entity.QPost.post;

import com.gogym.gympay.dto.response.GetHistory;
import com.gogym.gympay.dto.response.QGetHistory;
import com.gogym.gympay.repository.GymPayHistoryRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class GymPayHistoryRepositoryImpl implements GymPayHistoryRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public Page<GetHistory> getAllHistoriesByGymPayId(Long id, Pageable pageable) {
    List<GetHistory> histories = queryFactory.select(new QGetHistory(
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
        .where(gymPayHistory.gymPay.id.eq(id))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory.select(gymPayHistory.count())
        .from(gymPayHistory)
        .where(gymPayHistory.gymPay.id.eq(id))
        .fetchOne();

    return new PageImpl<>(histories, pageable, total);
  }
}
