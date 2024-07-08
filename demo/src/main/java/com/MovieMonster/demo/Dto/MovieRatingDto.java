package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovieRatingDto {
    private String username;
    private int movieId;
    private int movieRating;
}