package com.gogym.post.repository.impl;

import com.gogym.post.dto.PostFilterRequestDto;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.QPost;
import com.gogym.post.filter.PostFilterBuilder;
import com.gogym.post.repository.PostRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
@Primary
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  private final PostFilterBuilder postFilterBuilder;

  @Override
  public Page<Post> findAllWithFilter(List<Long> regionIds,
      PostFilterRequestDto postFilterRequestDto, Pageable sortedByDate) {

    QPost post = QPost.post;

    BooleanBuilder filter = postFilterBuilder.builderFilters(regionIds, postFilterRequestDto);

    List<Post> posts = queryFactory.selectFrom(post)
        .where(filter)
        .offset(sortedByDate.getOffset())
        .limit(sortedByDate.getPageSize())
        .orderBy(post.createdAt.desc())
        .fetch();

    Long total = Optional.ofNullable(queryFactory.select(post.count())
            .from(post)
            .where(filter)
            .fetchFirst())
        .orElse(0L);

    return new PageImpl<>(posts, sortedByDate, total);
  }
}