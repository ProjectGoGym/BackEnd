package com.gogym.post.repository;

import com.gogym.post.entity.Post;
import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

  Page<Post> findAllWithFilter(BooleanBuilder filter, Pageable sortedByDate);
}
