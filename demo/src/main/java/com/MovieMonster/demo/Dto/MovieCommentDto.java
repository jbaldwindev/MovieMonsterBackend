package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovieCommentDto {
    private int movieId;
    private int commentId;
    private int likeCount;
    private Boolean currentUserLiked;
    private String username;
    private String userIconPath;
    private String comment;
}
