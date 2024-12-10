package com.gogym.post.dto;

import com.gogym.post.type.FilterMonthsType;
import com.gogym.post.type.FilterPtType;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;

public record PostFilterRequestDto(

    PostType postType,
    MembershipType membershipType,
    PostStatus status,
    FilterMonthsType monthsType,
    FilterPtType ptType

) {}