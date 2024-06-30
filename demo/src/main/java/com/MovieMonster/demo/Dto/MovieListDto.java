package com.MovieMonster.demo.Dto;

import com.MovieMonster.demo.Models.Movie;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class MovieListDto {
    private ArrayList<Movie> movieList;
    private ArrayList<MovieSearchDto> movieSearchList;
}
