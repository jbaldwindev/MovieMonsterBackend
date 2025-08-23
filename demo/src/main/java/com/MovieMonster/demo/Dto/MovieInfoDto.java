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
    private String tagline;
    private String releaseDate;
    private ArrayList<String> genres;
    private ArrayList<String> productionCompanies;
    private int runtime;
    private ArrayList<CastMemberDto> cast;

}
