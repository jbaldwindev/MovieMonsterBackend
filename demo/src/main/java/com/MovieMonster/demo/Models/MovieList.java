package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MovieList")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(mappedBy = "movieList")
    private List<MovieRating> movieRatingList = new ArrayList<>();
}
