package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentLikeDto {
    private String username;
    private int commentId;
}
