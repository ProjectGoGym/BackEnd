package com.gogym.post.service;

import com.gogym.member.entity.Member;
import com.gogym.post.entity.Post;

public interface PostQueryService {
  Member getPostAuthor(Long postId);
  Post findById(Long postId);
  boolean isWished(Post post, Long memberId);
}
