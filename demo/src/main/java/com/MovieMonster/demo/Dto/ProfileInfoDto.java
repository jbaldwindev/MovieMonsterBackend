package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ProfileInfoDto {
    String username;
    LocalDateTime joinDate;
    int friendCount;
    List<Integer> favoriteIds;
    String bio;
}
