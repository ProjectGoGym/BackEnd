package com.gogym.gympay.repository.impl;

import static com.gogym.chat.entity.QChatRoom.chatRoom;
import static com.gogym.gympay.entity.QTransaction.transaction;
import static com.gogym.post.entity.QGym.gym;
import static com.gogym.post.entity.QPost.post;

import com.gogym.gympay.entity.constant.TransactionStatus;
import com.gogym.gympay.repository.TransactionRepositoryCustom;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.dto.QPostPageResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<PostPageResponseDto> getMyTransactions(Long memberId, LocalDateTime startDate,
      LocalDateTime endDate, Pageable pageable, String type) {
    BooleanBuilder condition = new BooleanBuilder();

    condition.and(transaction.status.eq(TransactionStatus.COMPLETED));

    if ("SELL".equalsIgnoreCase(type)) {
      condition.and(transaction.seller.id.eq(memberId));
    } else if ("BUY".equalsIgnoreCase(type)) {
      condition.and(transaction.buyer.id.eq(memberId));
    }

    if (startDate != null && endDate != null) {
      condition.and(post.createdAt.between(startDate, endDate));
    }

    List<PostPageResponseDto> results = queryFactory.select(new QPostPageResponseDto(
            post.id,
            post.title,
            post.status,
            post.amount,
            post.imageUrl1,
            post.imageUrl2,
            post.imageUrl3,
            gym.gymName,
            post.createdAt,
            post.wishCount,
            post.author.nickname
        ))
        .from(post)
        .leftJoin(post.gym, gym)
        .leftJoin(post.chatRoom, chatRoom)
        .leftJoin(transaction)
        .on(chatRoom.transactionId.eq(transaction.id))
        .where(condition)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(transaction.completedAt.desc())
        .fetch();

    Long total = queryFactory.select(post.count())
        .from(post)
        .leftJoin(post.chatRoom, chatRoom)
        .leftJoin(transaction).on(chatRoom.transactionId.eq(transaction.id))
        .where(condition)
        .fetchOne();

    return new PageImpl<>(results, pageable, total);
  }
}
