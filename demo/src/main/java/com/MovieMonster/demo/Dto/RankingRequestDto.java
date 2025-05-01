package com.MovieMonster.demo.Dto;

import com.MovieMonster.demo.Models.RankingDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingRequestDto {
    String username;
    int movieId;
    RankingDirection rankingDirection;
}
