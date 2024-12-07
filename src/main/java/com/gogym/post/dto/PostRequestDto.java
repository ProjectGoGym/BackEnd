package com.gogym.post.dto;

import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PostRequestDto(

    @NotBlank(message = "제목은 필수 입력입니다.")
    String title,
    @NotBlank(message = "내용은 필수 입력입니다.")
    String content,
    @NotNull(message = "게시글 상태를 설정해 주세요.")
    PostType postType,
    @NotNull(message = "회원권 상태를 설정해 주세요.")
    MembershipType membershipType,
    @Future(message = "현재 날짜 이후만 설정이 가능합니다.")
    LocalDate expirationDate,
    Long remainingSessions,
    @NotNull
    @Min(value = 1000, message = "최소 1000원 이상 입력해 주세요.")
    Long amount,
    String imageUrl1,
    String imageUrl2,
    String imageUrl3,
    @NotBlank(message = "헬스장을 등록해 주세요.")
    String gymName,
    Double latitude,
    Double longitude,
    String gymKakaoUrl,
    String city,
    String district

) {}