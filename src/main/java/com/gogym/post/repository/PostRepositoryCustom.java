package com.gogym.post.repository;

import com.gogym.post.dto.PostFilterRequestDto;
import com.gogym.post.entity.Post;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

  Page<Post> findAllWithFilter(List<Long> regionIds, PostFilterRequestDto postFilterRequestDto, Pageable sortedByDate);
  
}