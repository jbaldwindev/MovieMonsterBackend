package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int movieId;
    private String originalTitle;
    private String posterPath;
    private String overview;
    @OneToMany(mappedBy = "movieComment")
    private List<MovieComment> movieCommentList = new ArrayList<>();
}
