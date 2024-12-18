package com.gogym.member.entity;

import jakarta.persistence.*;
import lombok.*;
import com.gogym.common.entity.BaseIdEntity;

@Entity
@Table(name = "ban_nickname")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BanNickname extends BaseIdEntity {

  @Column(name = "banned_nickname", nullable = false)
  private String bannedNickname;

}
