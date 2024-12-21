package com.gogym.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {
  
  private final PostRepository postRepository;

  @Override
  public Member getPostAuthor(Long postId) {
    Post post = findById(postId);

    if (post.getAuthor() == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    } else {
      return post.getAuthor();
    }
  }

  @Override
  public Post findById(Long postId) {
    return postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
  }
  
}
