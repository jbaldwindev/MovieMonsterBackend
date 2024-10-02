package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovieCommentDto {
    private int movieId;
    private String username;
    private String comment;
}
