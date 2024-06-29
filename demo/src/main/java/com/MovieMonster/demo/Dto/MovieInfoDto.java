package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class MovieInfoDto {
    private int id;
    private String title;
    private String posterPath;
    private String overview;
    private String backdropPath;
    private ArrayList<CastMemberDto> cast;

}
