package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.MovieMonster.demo.Models.MovieComment;


import java.util.ArrayList;

@Data
@NoArgsConstructor
public class CommentListDto {
    private int movieId;
    private ArrayList<MovieComment> commentList;
}
