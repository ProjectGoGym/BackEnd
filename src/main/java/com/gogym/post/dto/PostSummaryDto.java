package com.gogym.post.dto;

import com.gogym.post.type.PostStatus;

public record PostSummaryDto(
    Long postId,
    String title,
    Long amount,
    PostStatus status) {}
